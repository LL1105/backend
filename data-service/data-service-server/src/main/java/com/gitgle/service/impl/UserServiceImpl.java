package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.constant.RedisConstant;
import com.gitgle.convert.GithubUserConvert;
import com.gitgle.dao.User;
import com.gitgle.response.GithubUser;
import com.gitgle.service.UserService;
import com.gitgle.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
* @author maojunjun
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-10-28 20:05:34
*/
@Service
@Slf4j
public class UserServiceImpl implements UserService{

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void writeGithubUser2User(List<GithubUser> githubUserList) {
        for(GithubUser githubUser : githubUserList){
            // 分布式锁防止重复插入
            RLock lock = redissonClient.getLock(RedisConstant.GITHUB_USER_LOCK + githubUser.getLogin());
            try{
                lock.lock(5, TimeUnit.SECONDS);
                // 先根据login查询数据库中是否存在
                User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getLogin, githubUser.getLogin()));
                if (ObjectUtils.isNotEmpty(user)) {
                    // 更新
                    user.setUpdateTime(LocalDateTime.now());
                    user.setAvatarUrl(githubUser.getAvatarUrl());
                    user.setLocationn(githubUser.getLocation());
                    user.setBio(githubUser.getBio());
                    user.setCompany(githubUser.getCompany());
                    user.setAccountId(githubUser.getId());
                    user.setCreatedAt(githubUser.getCreatedAt());
                    user.setEmail(githubUser.getEmail());
                    user.setPublicRepos(githubUser.getPublicRepos());
                    user.setFollowers(githubUser.getFollowers());
                    user.setFollowing(githubUser.getFollowing());
                    user.setHtmlUrl(githubUser.getHtmlUrl());
                    userMapper.updateById(user);
                    return;
                }
                // 如果没有则入库
                user = GithubUserConvert.convert2User(githubUser);
                userMapper.insert(user);
            }catch (Exception e){
                log.error("writeGithubUser2User Exception: {}", e.getMessage());
            }finally {
                lock.unlock();
            }
        }
    }

    @Override
    public GithubUser readGithubUser2GithubUser(String login) {
        try{
            User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getLogin, login));
            if(ObjectUtils.isEmpty(user)){
                return null;
            }
            return GithubUserConvert.convert(user);
        }catch (Exception e){
            log.error("readGithubUser2GithubUser Exception: {}", e.getMessage());
            return null;
        }
    }
}




