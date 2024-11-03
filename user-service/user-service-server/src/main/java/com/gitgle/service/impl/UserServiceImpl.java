package com.gitgle.service.impl;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.constant.RedisConstant;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.entity.Domain;
import com.gitgle.entity.Nation;
import com.gitgle.entity.UserDomain;
import com.gitgle.mapper.*;
import com.gitgle.response.*;
import com.gitgle.result.Result;
import com.gitgle.entity.User;

import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubFollowingService;
import com.gitgle.service.GithubRepoService;
import com.gitgle.service.GithubUserInfo;
import com.gitgle.service.GithubUserService;
import com.gitgle.service.req.*;
import com.gitgle.service.resp.*;
import com.gitgle.utils.Md5Util;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.ibatis.annotations.Param;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@DubboService
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements com.gitgle.service.UserService  {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    UserMapper userMapper;

    @Resource
    private UserDomainMapper userDomainMapper;

    @Resource
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String from;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    DomainMapper domainMapper;

    @DubboReference
    GithubUserService githubUserService;

    @Resource
    NationMapper nationMapper;

    @Resource
    GithubUserMapper githubUserMapper;

    @DubboReference
    GithubFollowingService githubFollowingService;

    @DubboReference
    GithubRepoService githubRepoService;


    @Override
    public String getRank(Integer userId) {
        //test ci
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", userId);
        User user = userMapper.selectOne(queryWrapper);
        return user.getTalentRank();
    }

    @Override
    public Result getUserInfo() {
        //展示所有的个人信息，包括系统内部的和github相关的
        Object loginId = StpUtil.getLoginId();
        User user = userMapper.selectById(loginId.toString());
        if(user != null) {
            UserInfoResp resp = new UserInfoResp();
            if(!StringUtils.isEmpty(user.getLogin())) {
                String login = user.getLogin();
                GithubUserInfo githubUserInfo = getGithubUserInfoBylogin(login);
                resp.setGithubUserInfo(githubUserInfo);
            }

            BeanUtils.copyProperties(user, resp);
            return Result.Success(resp);
        }
        return Result.Failed("用户不存在");
    }

    public GithubUserInfo getGithubUserInfoBylogin(String login) {
        //组装follow和个人信息
        GithubUserInfo info = new GithubUserInfo();
        RpcResult<GithubUser> userByLogin = githubUserService.getUserByLogin(login);
        RpcResult<GithubFollowersResponse> followers = githubFollowingService.getFollowersByDeveloperId(login);
        RpcResult<GithubFollowingResponse> following = githubFollowingService.listUserFollowingByDeveloperId(login);
        if(userByLogin.getCode().equals(RpcResultCode.SUCCESS)) {
            info.setGithubUser(userByLogin.getData());
        }

        if(followers.getCode().equals(RpcResultCode.SUCCESS)) {
            info.setGithubFollowers(followers.getData());
        }

        if(following.getCode().equals(RpcResultCode.SUCCESS)) {
            info.setGithubFollowing(following.getData());
        }
        return info;
    }

    @Override
    public Result sendMimeMail(String email) {
        if(StringUtils.isEmpty(email)) return Result.Failed("邮箱地址不能为空");
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setSubject("欢迎来到gitgle，您的验证码是：");
            String code = randomCode();
            logger.info("邮箱验证码: {}", code);
            stringRedisTemplate.opsForValue().set(RedisConstant.REGISTER_CODE_PREFIX + email, code,3, TimeUnit.MINUTES);
            mailMessage.setText("您收到的验证码是：" + code);
            mailMessage.setTo(email);
            mailMessage.setFrom(from);
            mailSender.send(mailMessage);
            return Result.Success();
        } catch (Exception e) {
            logger.error("发送邮件时发生异常", e);
            return Result.Failed("邮件发送失败，请稍后再试");
        }
    }

    @Override
    @Transactional
    public Result register(RegisterReq req) {
        User one = this.getOne(Wrappers.lambdaQuery(User.class).eq(User::getEmail, req.getEmail()));
        if(one != null) return Result.Failed("邮箱已经存在");

        String code = stringRedisTemplate.opsForValue().get(RedisConstant.REGISTER_CODE_PREFIX + req.getEmail());

        if(StringUtils.isEmpty(code)) return Result.Failed("验证码已过期，请重新发送");

        if(!code.equals(req.getCode())) return Result.Failed("验证码无效");

        String password = Md5Util.md5(req.getPassword(), Md5Util.md5Key);
        req.setPassword(password);

        User user = new User();

        //搜索github用户，添加头像的url
        if(!StringUtils.isEmpty(req.getLogin())) {
            RpcResult<GithubUser> userByLogin = githubUserService.getUserByLogin(req.getLogin());
            if(userByLogin.getCode().equals(RpcResultCode.SUCCESS)) {
                GithubUser githubUser = userByLogin.getData();
                user.setAvatar(githubUser.getAvatarUrl());
            }
        }

        BeanUtils.copyProperties(req, user);
        userMapper.insert(user);
        RegisterResp resp = new RegisterResp();
        BeanUtils.copyProperties(user, resp);
        return Result.Success(resp);
    }

    public String randomCode() {
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            str.append(random.nextInt(10));
        }
        return str.toString();
    }

    @Override
    public Result login(String email, String password)  {
        User user = this.getOne(Wrappers.lambdaQuery(User.class).eq(User::getEmail, email));
        if(user == null) return Result.Failed("账号或密码错误");

        boolean result = Md5Util.passwordVerify(password, user.getPassword(), Md5Util.md5Key);

        if(result) {
            StpUtil.login(user.getId());
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            LoginResp resp = new LoginResp();
            resp.setUserId(user.getId());
            resp.setUserName(user.getUsername());
            resp.setToken(tokenInfo.getTokenValue());
            return Result.Success(resp);
        }

        return Result.Failed("账号或密码错误");
    }

    @Override
    public SaResult logout() {
        StpUtil.logout();
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        Boolean isLogin = tokenInfo.isLogin;
        if(!isLogin) return SaResult.ok();
        return SaResult.error("退出失败");
    }

    @Override
    public Result search(Integer page, Integer size, SearchReq searchReq) {
        SearchResp resp = new SearchResp();

        Integer current = (page - 1) * size;

        List<SearchUser> searchList = githubUserMapper.searchByCondition(current, size, searchReq);
//        for (SearchUser searchUser : searchList) {
//            List<String> domains = new ArrayList<>();
//            //组装login所在的领域
//            String userLogin = searchUser.getLogin();
//            QueryWrapper<UserDomain> userDomainQueryWrapper = new QueryWrapper<>();
//            userDomainQueryWrapper.eq("login", userLogin);
//            List<UserDomain> userDomains = userDomainMapper.selectList(userDomainQueryWrapper);
//            for (UserDomain userDomain : userDomains) {
//                domains.add(userDomain.getDomain());
//            }
//            searchUser.setDomains(domains);
//        }
        //查全部条数
        Integer count = githubUserMapper.searchCount(searchReq);
        resp.setSearchUsers(searchList);
        resp.setPage(page);
        resp.setPageSize(size);
        resp.setTotalPage((long) Math.round(((count / size) + 0.5)));
        return Result.Success(resp);
    }

    @Override
    public Result getNation() {
        List<Nation> nations = nationMapper.selectList(new QueryWrapper<>());
        return Result.Success(nations);
    }

    @Override
    @Transactional
    public Result changeUserInfo(ChangeUserInfoReq req) {
        //从token里面解析id
        Object loginId = StpUtil.getLoginId();
        User user = userMapper.selectById(loginId.toString());
        if(user == null) return Result.Failed("账号不存在");

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", user.getId())
                .set("username", req.getUsername())
                .set("email", req.getEmail())
                .set("github_id", req.getLogin());

        //搜索github用户，添加头像的url
        if(!StringUtils.isEmpty(req.getLogin())) {
            RpcResult<GithubUser> userByLogin = githubUserService.getUserByLogin(req.getLogin());
            if(userByLogin.getCode().equals(RpcResultCode.SUCCESS)) {
                GithubUser githubUser = userByLogin.getData();
                updateWrapper.set("avatar", githubUser.getAvatarUrl());
            }
        }

        int update = userMapper.update(null, updateWrapper);
        if(update != 1) return Result.Failed();

        User updateUser = userMapper.selectById(user.getId());
        ChangeUserInfoResp resp = new ChangeUserInfoResp();
        BeanUtils.copyProperties(updateUser, resp);
        return Result.Success(resp);
    }

    @Override
    public Result showUserInfo(String login) {
        ShowUserInfoResp resp = new ShowUserInfoResp();
        RpcResult<GithubUser> userByLogin = githubUserService.getUserByLogin(login);
        try {
            if(userByLogin.getCode().equals(RpcResultCode.SUCCESS)) {
                GithubUser data = userByLogin.getData();
                resp.setGithubUser(data);
                RpcResult<GithubReposResponse> rpcResult = githubRepoService.listUserRepos(data.getLogin());
                //组装开发者的仓库信息
                if(rpcResult.getCode().equals(RpcResultCode.SUCCESS)) {
                    GithubReposResponse githubReposResponse = rpcResult.getData();
                    List<GithubRepos> githubProjectList = githubReposResponse.getGithubProjectList();
                    resp.setGithubReposList(githubProjectList);
                }
                return Result.Success(resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.Failed("详情获取失败");
        }

        return Result.Failed("详情获取失败");
    }

    @Override
    @Transactional
    public Result changePassword(ChangePasswordReq req) {
        Object loginId = StpUtil.getLoginId();
        User user = userMapper.selectById(loginId.toString());
        if(user == null) return Result.Failed("账号不存在");

        String oldPassword = req.getOldPassword();
        String newPassword = req.getNewPassword();

        if(!Md5Util.passwordVerify(oldPassword, user.getPassword(), Md5Util.md5Key)) {
            return Result.Failed("旧密码错误");
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", user.getId()).set("password", Md5Util.md5(newPassword, Md5Util.md5Key));
        userMapper.update(null, updateWrapper);
        return Result.Success();
    }

    @Override
    public RpcResult<Long> getUserCountInDomain(Integer domainId) {
        RpcResult<Long> result = new RpcResult<>();
        try{
            Long userCount = userDomainMapper.selectCount(Wrappers.lambdaQuery(UserDomain.class).select(UserDomain::getId).eq(UserDomain::getDomainId, domainId));
            result.setCode(RpcResultCode.SUCCESS);
            result.setData(userCount);
            return result;
        }catch (Exception e){
            result.setCode(RpcResultCode.FAILED);
            return result;
        }
    }

}