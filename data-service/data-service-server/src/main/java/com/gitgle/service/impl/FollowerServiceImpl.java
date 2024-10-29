package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.dao.Follower;
import com.gitgle.response.GithubFollowers;
import com.gitgle.service.FollowerService;
import com.gitgle.mapper.FollowerMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author maojunjun
* @description 针对表【follower】的数据库操作Service实现
* @createDate 2024-10-28 21:17:16
*/
@Service
public class FollowerServiceImpl implements FollowerService{

    @Resource
    private FollowerMapper followerMapper;

    @Override
    public List<GithubFollowers> readFollower2GithubFollowers(String login) {
        List<Follower> followerList = followerMapper.selectList(Wrappers.lambdaQuery(Follower.class).eq(Follower::getUsername, login));
        return followerList.stream().map(follower -> {
            GithubFollowers githubFollowers = new GithubFollowers();
            githubFollowers.setLogin(follower.getLogin());
            githubFollowers.setAvatarUrl(follower.getAvatarUrl());
            return githubFollowers;
        }).collect(Collectors.toList());
    }

    @Override
    public void writeGithubFollower2Follower(GithubFollowers githubFollowers, String login) {
        Follower follower = followerMapper.selectOne(Wrappers.lambdaQuery(Follower.class).eq(Follower::getUsername, login).eq(Follower::getLogin,githubFollowers.getLogin()));
        if(ObjectUtils.isNotEmpty(follower)){
            return;
        }
        follower = new Follower();
        follower.setLogin(githubFollowers.getLogin());
        follower.setAvatarUrl(githubFollowers.getAvatarUrl());
        follower.setUsername(login);
        follower.setCreateTime(LocalDateTime.now());
        follower.setUpdateTime(LocalDateTime.now());
        followerMapper.insert(follower);
    }

    @Override
    public List<GithubFollowers> readFollowing2GithubFollowing(String login) {
        List<Follower> followerList = followerMapper.selectList(Wrappers.lambdaQuery(Follower.class).eq(Follower::getLogin, login));
        return followerList.stream().map(follower -> {
            GithubFollowers githubFollowers = new GithubFollowers();
            githubFollowers.setLogin(follower.getUsername());
            githubFollowers.setAvatarUrl(follower.getAvatarUrl());
            return githubFollowers;
        }).collect(Collectors.toList());
    }

    @Override
    public void writeGithubFollowing2Following(GithubFollowers githubFollowers, String login) {
        Follower follower = followerMapper.selectOne(Wrappers.lambdaQuery(Follower.class).eq(Follower::getLogin, login).eq(Follower::getUsername,githubFollowers.getLogin()));
        if(ObjectUtils.isNotEmpty(follower)){
            return;
        }
        follower = new Follower();
        follower.setLogin(login);
        follower.setAvatarUrl(githubFollowers.getAvatarUrl());
        follower.setUsername(githubFollowers.getLogin());
        follower.setCreateTime(LocalDateTime.now());
        follower.setUpdateTime(LocalDateTime.now());
        followerMapper.insert(follower);
    }


}




