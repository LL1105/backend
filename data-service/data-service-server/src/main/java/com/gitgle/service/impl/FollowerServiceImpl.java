package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.constant.RedisConstant;
import com.gitgle.convert.GithubFollowerConvert;
import com.gitgle.convert.GithubFollowingConvert;
import com.gitgle.dao.Follower;
import com.gitgle.response.GithubFollowers;
import com.gitgle.response.GithubFollowing;
import com.gitgle.service.FollowerService;
import com.gitgle.mapper.FollowerMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FollowerServiceImpl implements FollowerService{

    @Resource
    private FollowerMapper followerMapper;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public List<GithubFollowers> readFollower2GithubFollowers(String login) {
        List<Follower> followerList = followerMapper.selectList(Wrappers.lambdaQuery(Follower.class).eq(Follower::getFollowingLogin, login));
        return followerList.stream().map(follower -> {
            GithubFollowers githubFollowers = new GithubFollowers();
            githubFollowers.setLogin(follower.getFollowerLogin());
            githubFollowers.setAvatarUrl(follower.getFollowerAvatarUrl());
            githubFollowers.setId(follower.getFollowerId());
            return githubFollowers;
        }).collect(Collectors.toList());
    }

    @Override
    public void writeGithubFollower2Follower(GithubFollowers githubFollowers, String login) {
        RLock lock = redissonClient.getLock(RedisConstant.GITHUB_FOLLOWER_LOCK + login + ":" + githubFollowers.getLogin());
        try {
            lock.lock(5, TimeUnit.SECONDS);
            Follower follower = followerMapper.selectOne(Wrappers.lambdaQuery(Follower.class).eq(Follower::getFollowingLogin, login).eq(Follower::getFollowerLogin,githubFollowers.getLogin()));
            if(ObjectUtils.isNotEmpty(follower)){
                // 更新
                follower.setUpdateTime(LocalDateTime.now());
                follower.setFollowerLogin(githubFollowers.getLogin());
                follower.setFollowingLogin(login);
                follower.setFollowerAvatarUrl(githubFollowers.getAvatarUrl());
                follower.setFollowerId(githubFollowers.getId());
                followerMapper.updateById(follower);
                return;
            }
            follower = GithubFollowerConvert.convert2Follower(githubFollowers, login);
            followerMapper.insert(follower);
        }catch (Exception e){
            log.error("Github Follower Write Exception: {}", e.getMessage());
        }finally {
            lock.unlock();
        }
    }

    @Override
    public List<GithubFollowing> readFollowing2GithubFollowing(String login) {
        List<Follower> followerList = followerMapper.selectList(Wrappers.lambdaQuery(Follower.class).eq(Follower::getFollowerLogin, login));
        return followerList.stream().map(follower -> {
            GithubFollowing githubFollowing = new GithubFollowing();
            githubFollowing.setLogin(follower.getFollowingLogin());
            githubFollowing.setAvatarUrl(follower.getFollowingAvatarUrl());
            githubFollowing.setId(follower.getFollowingId());
            return githubFollowing;
        }).collect(Collectors.toList());
    }

    @Override
    public void writeGithubFollowing2Following(GithubFollowing githubFollowing, String login) {
        RLock lock = redissonClient.getLock(RedisConstant.GITHUB_FOLLOWING_LOCK + login + ":" + githubFollowing.getLogin());
        try {
            lock.lock(5, TimeUnit.SECONDS);
            Follower follower = followerMapper.selectOne(Wrappers.lambdaQuery(Follower.class).eq(Follower::getFollowerLogin, login).eq(Follower::getFollowingLogin,githubFollowing.getLogin()));
            if(ObjectUtils.isNotEmpty(follower)){
                // 更新
                follower.setFollowingId(githubFollowing.getId());
                follower.setFollowingAvatarUrl(githubFollowing.getAvatarUrl());
                follower.setFollowingLogin(githubFollowing.getLogin());
                follower.setFollowerLogin(login);
                follower.setUpdateTime(LocalDateTime.now());
                return;
            }
            follower = GithubFollowingConvert.convert2Following(githubFollowing, login);
            followerMapper.insert(follower);
        }catch (Exception e){
            log.error("Github Following Write Exception: {}", e.getMessage());
        }finally {
            lock.unlock();
        }
    }

}




