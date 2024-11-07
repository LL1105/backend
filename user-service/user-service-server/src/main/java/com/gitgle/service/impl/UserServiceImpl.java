package com.gitgle.service.impl;


import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.constant.RedisConstant;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.convert.GithubUserConvert;
import com.gitgle.entity.Domain;
import com.gitgle.entity.Nation;
import com.gitgle.entity.UserDomain;
import com.gitgle.mapper.*;
import com.gitgle.response.*;
import com.gitgle.result.Result;
import com.gitgle.entity.User;

import com.gitgle.result.RpcResult;
import com.gitgle.service.*;
import com.gitgle.service.UserDomainService;
import com.gitgle.service.req.*;
import com.gitgle.service.resp.*;
import com.gitgle.utils.Md5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@DubboService
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements com.gitgle.service.UserService  {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserDomainMapper userDomainMapper;

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String from;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DomainMapper domainMapper;

    @DubboReference
    private GithubUserService githubUserService;

    @Resource
    private NationMapper nationMapper;

    @Resource
    private GithubUserMapper githubUserMapper;

    @DubboReference
    private GithubFollowingService githubFollowingService;

    @DubboReference
    private GithubRepoService githubRepoService;

    @DubboReference
    private TalentRankService talentRankService;

    @DubboReference
    private NationService nationService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserDomainService userDomainService;

    @DubboReference
    private RpcDomainService rpcDomainService;


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
            UserInfoResp resp = new UserInfoResp();
            String userLogin = user.getLogin();
            if(!StringUtils.isEmpty(userLogin)) {
                Result result = showUserInfo(userLogin);
                ShowUserInfoResp data = (ShowUserInfoResp) result.getData();
                GithubUserResp githubUser = data.getGithubUser();
                GithubUserInfo githubUserInfo = getGithubUserInfoBylogin(userLogin);
                githubUserInfo.setGithubUser(githubUser);
                resp.setGithubUserInfo(githubUserInfo);
            }

            BeanUtils.copyProperties(user, resp);
            return Result.Success(resp);
        }
        return Result.Failed("用户不存在");
    }

    public GithubUserInfo getGithubUserInfoBylogin(String login) {
        //组装follow
        GithubUserInfo info = new GithubUserInfo();
        RpcResult<GithubFollowersResponse> followers = githubFollowingService.getFollowersByDeveloperId(login);
        RpcResult<GithubFollowingResponse> following = githubFollowingService.listUserFollowingByDeveloperId(login);

        if(followers.getCode().equals(RpcResultCode.SUCCESS)) {
            List<GithubUser> githubUserList = new ArrayList<>();
            GithubFollowersResponse data = followers.getData();
            List<GithubFollowers> githubFollowersList = data.getGithubFollowersList();
            for (GithubFollowers githubFollowers : githubFollowersList) {
                String followerLogin = githubFollowers.getLogin();
                RpcResult<GithubUser> userByLogin1 = githubUserService.getUserByLogin(followerLogin);
                if(userByLogin1.getCode().equals(RpcResultCode.SUCCESS)) {
                    githubUserList.add(userByLogin1.getData());
                }
            }
            info.setGithubFollowers(githubUserList);
        }

        if(following.getCode().equals(RpcResultCode.SUCCESS)) {
            List<GithubUser> githubUserList = new ArrayList<>();
            GithubFollowingResponse data = following.getData();
            List<GithubFollowing> githubFollowingList = data.getGithubFollowingList();
            for (GithubFollowing githubFollowing : githubFollowingList) {
                String githubFollowingLogin = githubFollowing.getLogin();
                RpcResult<GithubUser> userByLogin1 = githubUserService.getUserByLogin(githubFollowingLogin);
                if(userByLogin1.getCode().equals(RpcResultCode.SUCCESS)) {
                    githubUserList.add(userByLogin1.getData());
                }
            }
            info.setGithubFollowing(githubUserList);
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

            //先查github_user表是否存在这个login信息
            QueryWrapper<com.gitgle.entity.GithubUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("login", user.getLogin());
            com.gitgle.entity.GithubUser selectOne = githubUserMapper.selectOne(queryWrapper);
            if(selectOne == null) {
                //新增用户进github_user表
                com.gitgle.entity.GithubUser githubUser = new com.gitgle.entity.GithubUser();
                githubUser.setLogin(user.getLogin());
                githubUserMapper.insert(githubUser);
            }

            //更新 github_user表数据
            CompletableFuture.runAsync(()->{
                RpcResult<GithubUser> userByLogin = githubUserService.getUserByLogin(user.getLogin());
                if(userByLogin.getCode().equals(RpcResultCode.FAILED)) {
                    return;
                }
                GithubUser data = userByLogin.getData();
                QueryWrapper<com.gitgle.entity.GithubUser> githubUserQueryWrapper = new QueryWrapper<>();
                githubUserQueryWrapper.eq("login", data.getLogin());
                com.gitgle.entity.GithubUser githubUser = githubUserMapper.selectOne(githubUserQueryWrapper);
                if(StringUtils.isBlank(githubUser.getAvatar())){
                    githubUser.setAvatar(data.getAvatarUrl());
                }
                if(ObjectUtils.isEmpty(githubUser.getTalentRank())){
                    RpcResult<String> talentrankByDeveloperId = talentRankService.getTalentrankByDeveloperId(data.getLogin());
                    if(RpcResultCode.SUCCESS.equals(talentrankByDeveloperId.getCode())){
                        githubUser.setTalentRank(new BigDecimal(talentrankByDeveloperId.getData()));
                    }
                }
                if(StringUtils.isBlank(githubUser.getNation())){
                    RpcResult<NationResponse> nationByDeveloperId = nationService.getNationByDeveloperId(data.getLogin());
                    if(RpcResultCode.SUCCESS.equals(nationByDeveloperId.getCode())){
                        githubUser.setNation(nationByDeveloperId.getData().getNation());
                        githubUser.setNationConfidence(new BigDecimal(nationByDeveloperId.getData().getConfidence()));
                        githubUser.setNationEnglish(nationByDeveloperId.getData().getNationEnglish());
                    }
                }
                githubUserMapper.updateById(githubUser);
            });
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
    public Result search(Integer page, Integer size, SearchReq req) {
        // 如果没有查询条件则走缓存
        if(is2SearchUserCache(req, page)){
            Set<SearchUser> searchResps = redisTemplate.opsForZSet().range(RedisConstant.GITHUB_USER_RANK, (page-1)*size, page*size-1);
            if(ObjectUtils.isNotEmpty(searchResps)){
                SearchResp resp = new SearchResp();
                List<SearchUser> searchUserList = searchResps.stream().collect(Collectors.toList());
                setSearchRespParams(resp, Long.valueOf(page), Long.valueOf(size),
                        Long.valueOf(redisTemplate.opsForValue().get(RedisConstant.GITHUB_USER_RANK_PAGES).toString()), searchUserList);
                return Result.Success(resp);
            }
        }
        SearchResp resp = new SearchResp();
        List<SearchUser> searchUserList = new ArrayList<>();
        // 如果开发者login不为空，则根据login查询用户
        if(StringUtils.isNotEmpty(req.getLogin())){
            com.gitgle.entity.GithubUser githubUser = githubUserMapper.selectOne(Wrappers.lambdaQuery(com.gitgle.entity.GithubUser.class).eq(com.gitgle.entity.GithubUser::getLogin, req.getLogin()));
            if(ObjectUtils.isEmpty(githubUser)){
                setSearchRespParams(resp, Long.valueOf(page), Long.valueOf(size), 1L, searchUserList);
                return Result.Success(resp);
            }
            SearchUser searchUser = GithubUserConvert.convert2SearchUser(githubUser, userDomainService.
                    getUserDomainByLogin(githubUser.getLogin()).stream().
                    map(UserDomain::getDomain).collect(Collectors.toList()));
            searchUserList.add(searchUser);
            setSearchRespParams(resp, Long.valueOf(page), Long.valueOf(size), 1L, searchUserList);
            return Result.Success(resp);
        }
        // 如果领域搜索条件不为空，则查询领域下的用户
        if(StringUtils.isNotEmpty(req.getDomain())){
            // 先模糊匹配领域
            List<Domain> domains = domainMapper.selectList(Wrappers.lambdaQuery(Domain.class).like(Domain::getDomain, req.getDomain()));
            if(ObjectUtils.isEmpty(domains)){
                setSearchRespParams(resp, Long.valueOf(page), Long.valueOf(size), 1L, searchUserList);
                return Result.Success(resp);
            }
            // 分也查询用户领域
            IPage<UserDomain> userDomainByDomainId = userDomainService.pageUserDomainByDomainId(domains.stream().map(Domain::getId).collect(Collectors.toList()), page, size);
            // 遍历查询用户
            for(UserDomain userDomain: userDomainByDomainId.getRecords()){
                com.gitgle.entity.GithubUser githubUser = githubUserMapper.selectOne(Wrappers.lambdaQuery(com.gitgle.entity.GithubUser.class).eq(com.gitgle.entity.GithubUser::getLogin, userDomain.getLogin()));
                // 根据Nation 过滤
                if(ObjectUtils.isEmpty(githubUser) ||
                        (StringUtils.isNotEmpty(req.getNation())
                                && !req.getNation().equals(githubUser.getNation()))){
                    continue;
                }
                SearchUser searchUser = GithubUserConvert.convert2SearchUser(githubUser, userDomainService.
                        getUserDomainByLogin(githubUser.getLogin()).stream().
                        map(UserDomain::getDomain).collect(Collectors.toList()));
                searchUserList.add(searchUser);
            }
            setSearchRespParams(resp, userDomainByDomainId.getCurrent(), userDomainByDomainId.getSize(),
                    Math.round(((searchUserList.size() / size) + 0.5)), searchUserList);
        }else if(StringUtils.isNotEmpty(req.getNation())){
            // 如果国家不为空，则查询该国家下的用户
            Page<com.gitgle.entity.GithubUser> githubUserPage = new Page<>(page, size);
            Page<com.gitgle.entity.GithubUser> githubUserPage1 = githubUserMapper.selectPage(githubUserPage,
                    Wrappers.lambdaQuery(com.gitgle.entity.GithubUser.class)
                            .eq(com.gitgle.entity.GithubUser::getNation, req.getNation())
                            .orderBy(true, false, com.gitgle.entity.GithubUser::getTalentRank));
            searchUserList = githubUserPage1.getRecords().stream().map(githubUser ->
                    GithubUserConvert.convert2SearchUser(githubUser, userDomainService.
                            getUserDomainByLogin(githubUser.getLogin()).stream().
                            map(UserDomain::getDomain).collect(Collectors.toList()))).collect(Collectors.toList());
            setSearchRespParams(resp, githubUserPage1.getCurrent(), githubUserPage1.getSize(),
                    githubUserPage1.getPages(), searchUserList);
        }else {
            // 条件都为空，则查询全部用户
            Page<com.gitgle.entity.GithubUser> githubUserPage = new Page<>(page, size);
            Page<com.gitgle.entity.GithubUser> githubUserPage1 = githubUserMapper.selectPage(githubUserPage, Wrappers.lambdaQuery(com.gitgle.entity.GithubUser.class).orderBy(true, false, com.gitgle.entity.GithubUser::getTalentRank));
            searchUserList = githubUserPage1.getRecords().stream().map(githubUser ->
                    GithubUserConvert.convert2SearchUser(githubUser, userDomainService.
                    getUserDomainByLogin(githubUser.getLogin()).stream().
                    map(UserDomain::getDomain).collect(Collectors.toList()))).collect(Collectors.toList());
            setSearchRespParams(resp, githubUserPage1.getCurrent(), githubUserPage1.getSize(),
                    githubUserPage1.getPages(), searchUserList);
        }
        if(is2SearchUserCache(req, page)){
            for(SearchUser searchUser: searchUserList){
                redisTemplate.opsForValue().set(RedisConstant.GITHUB_USER_RANK_PAGES, resp.getTotalPage());
                redisTemplate.expire(RedisConstant.GITHUB_USER_RANK_PAGES, 3, TimeUnit.DAYS);
                redisTemplate.opsForZSet().add(RedisConstant.GITHUB_USER_RANK, searchUser, -1*(Double.parseDouble(searchUser.getTalentRank())));
                redisTemplate.opsForZSet().removeRange(RedisConstant.GITHUB_USER_RANK, 60, -1);
            }
            redisTemplate.expire(RedisConstant.GITHUB_USER_RANK, 3, TimeUnit.DAYS);
        }
        return Result.Success(resp);
    }

    private Boolean is2SearchUserCache(SearchReq req, Integer page){
        return StringUtils.isBlank(req.getDomain())
                && StringUtils.isBlank(req.getNation())
                && StringUtils.isBlank(req.getLogin())
                && page <= 2;
    }

    private void setSearchRespParams(SearchResp resp, Long page, Long size, Long total, List<SearchUser> searchUsers){
        resp.setPage(page);
        resp.setPageSize(size);
        resp.setTotalPage(total);
        resp.setSearchUsers(searchUsers);
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
        if(!RpcResultCode.SUCCESS.equals(userByLogin.getCode())){
            return Result.Failed("获取用户详细信息失败");
        }
        QueryWrapper<com.gitgle.entity.GithubUser> githubUserQueryWrapper = new QueryWrapper<>();
        githubUserQueryWrapper.eq("login", login);
        com.gitgle.entity.GithubUser githubUser = githubUserMapper.selectOne(githubUserQueryWrapper);
        GithubUser data = userByLogin.getData();
        resp.setGithubUser(GithubUserConvert.convert2GithubUserResp(data, githubUser,
                userDomainService.getUserDomainByLogin(login).stream().map(UserDomain::getDomain).collect(Collectors.toList())));
        CompletableFuture.runAsync(()->{
            if(StringUtils.isBlank(githubUser.getAvatar())){
                githubUser.setAvatar(data.getAvatarUrl());
            }
            if(ObjectUtils.isEmpty(githubUser.getTalentRank())){
                RpcResult<String> talentrankByDeveloperId = talentRankService.getTalentrankByDeveloperId(data.getLogin());
                if(RpcResultCode.SUCCESS.equals(talentrankByDeveloperId.getCode())){
                    githubUser.setTalentRank(new BigDecimal(talentrankByDeveloperId.getData()));
                }
            }
            if(StringUtils.isBlank(githubUser.getNation())){
                RpcResult<NationResponse> nationByDeveloperId = nationService.getNationByDeveloperId(data.getLogin());
                if(RpcResultCode.SUCCESS.equals(nationByDeveloperId.getCode())){
                    githubUser.setNation(nationByDeveloperId.getData().getNation());
                    githubUser.setNationConfidence(new BigDecimal(nationByDeveloperId.getData().getConfidence()));
                    githubUser.setNationEnglish(nationByDeveloperId.getData().getNationEnglish());
                }
            }
            githubUserMapper.updateById(githubUser);
        });
        RpcResult<GithubReposResponse> rpcResult = githubRepoService.listUserRepos(data.getLogin());
        //组装开发者的仓库信息
        if(!RpcResultCode.SUCCESS.equals(rpcResult.getCode())) {
            return Result.Failed("获取用户仓库失败");
        }
        GithubReposResponse githubReposResponse = rpcResult.getData();
        List<GithubRepos> githubProjectList = githubReposResponse.getGithubProjectList();
        resp.setGithubReposList(githubProjectList);
        return Result.Success(resp);
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