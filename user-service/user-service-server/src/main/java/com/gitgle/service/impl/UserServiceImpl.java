package com.gitgle.service.impl;


import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.constant.RedisConstant;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.entity.Domain;
import com.gitgle.entity.Nation;
import com.gitgle.mapper.DomainMapper;
import com.gitgle.mapper.GithubUserMapper;
import com.gitgle.mapper.NationMapper;
import com.gitgle.mapper.UserMapper;
import com.gitgle.response.GithubUser;
import com.gitgle.result.Result;
import com.gitgle.entity.User;

import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubUserService;
import com.gitgle.service.req.RankReq;
import com.gitgle.service.req.RegisterReq;
import com.gitgle.service.req.SearchReq;
import com.gitgle.service.resp.*;
import com.gitgle.utils.Md5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
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


    @Override
    public String getRank(Integer userId) {
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
            String githubId = user.getGithubId();
            RpcResult<GithubUser> userByLogin = githubUserService.getUserByLogin(githubId);
            if(userByLogin.getCode().equals(RpcResultCode.SUCCESS)) {

            } else {

            }
            UserInfoResp resp = new UserInfoResp();
            BeanUtils.copyProperties(user, resp);
            return Result.Success(resp);
        }
        return Result.Failed("用户不存在");
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
    public Result register(RegisterReq req) {
        User one = this.getOne(Wrappers.lambdaQuery(User.class).eq(User::getEmail, req.getEmail()));
        if(one != null) return Result.Failed("邮箱已经存在");

        String code = stringRedisTemplate.opsForValue().get(RedisConstant.REGISTER_CODE_PREFIX + req.getEmail());

        if(StringUtils.isEmpty(code)) return Result.Failed("验证码已过期，请重新发送");

        if(!code.equals(req.getCode())) return Result.Failed("验证码无效");

        String password = Md5Util.md5(req.getPassword(), Md5Util.md5Key);
        req.setPassword(password);

        User user = new User();
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
    public Result getUsersByNation(String nation) {
        return null;
    }

    @Override
    public Result conditionCheckRank(Integer size, Integer current, RankReq req) {
        List<RankResp> rankResps = userMapper.selectUsersCondition(current, size, req);
        return Result.Success(rankResps);
    }

    @Override
    public Result search(SearchReq searchReq) {
        //req:领域名，地区id，github用户名
        //通过这套数据去查数据库，如果没有的话，再去通过github用户名去查service
        Integer domainId = null;
        String domain = searchReq.getDomain();

        LambdaQueryWrapper<Domain> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Domain::getDomain, domain);
        Domain selectOne = domainMapper.selectOne(queryWrapper);
        domainId = selectOne == null ? null : selectOne.getId();
        //暂时只能查询数据库里面存在的domain
        if(domainId != null) {
            searchReq.setDomain(String.valueOf(domainId));
        }
        List<SearchResp> searchList = githubUserMapper.searchByCondition(searchReq);
        return Result.Success(searchList);
        //数据库里面没有这个领域，那么直接调用rpc接口，找到github的这个领域

        //这里就直接去数据库查github的用户，根据domainId和nationId以及githubId的模糊查询
    }

    @Override
    public Result getNation() {
        List<Nation> nations = nationMapper.selectList(new QueryWrapper<>());
        return Result.Success(nations);
    }

    @Override
    public Result changeUserInfo() {
        return null;
    }
}