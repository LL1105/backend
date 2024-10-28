package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.dao.User;
import com.gitgle.response.GithubUser;
import com.gitgle.service.UserService;
import com.gitgle.mapper.UserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
* @author maojunjun
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-10-28 20:05:34
*/
@Service
public class UserServiceImpl implements UserService{

    @Resource
    private UserMapper userMapper;

    @Override
    public void writeGithubUser2User(GithubUser githubUser) {
        // 先根据login查询数据库中是否存在
        User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getLogin, githubUser.getLogin()));
        if (ObjectUtils.isNotEmpty(user)) {
            return;
        }
        // 如果没有则入库
        user = new User();
        user.setLogin(githubUser.getLogin());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setAvatarUrl(githubUser.getAvatarUrl());
        user.setLocationn(githubUser.getLocation());
        user.setBio(githubUser.getBio());
        user.setCompany(githubUser.getCompany());
        userMapper.insert(user);
    }

    @Override
    public GithubUser readGithubUser2GithubUser(String login) {
        User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getLogin, login));
        if(ObjectUtils.isEmpty(user)){
            return null;
        }
        GithubUser githubUser = new GithubUser();
        githubUser.setLogin(user.getLogin());
        githubUser.setAvatarUrl(user.getAvatarUrl());
        githubUser.setLocation(user.getLocationn());
        githubUser.setBio(user.getBio());
        githubUser.setCompany(user.getCompany());
        return githubUser;
    }


}




