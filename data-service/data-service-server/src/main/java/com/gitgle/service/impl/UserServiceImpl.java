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
import java.util.List;

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
    public void writeGithubUser2User(List<GithubUser> githubUserList) {
        for(GithubUser githubUser : githubUserList){
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
            user = new User();
            user.setLogin(githubUser.getLogin());
            user.setCreateTime(LocalDateTime.now());
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
            userMapper.insert(user);
        }
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
        githubUser.setId(user.getAccountId());
        githubUser.setHtmlUrl(user.getHtmlUrl());
        githubUser.setCreatedAt(user.getCreatedAt());
        githubUser.setEmail(user.getEmail());
        githubUser.setPublicRepos(user.getPublicRepos());
        githubUser.setFollowers(user.getFollowers());
        githubUser.setFollowing(user.getFollowing());
        return githubUser;
    }

    @Override
    public String getLoginByAccountId(Integer githubAccountId) {
        User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getAccountId, githubAccountId));
        if(ObjectUtils.isNotEmpty(user)){
            return user.getLogin();
        }
        return null;
    }
}




