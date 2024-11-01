package com.gitgle.convert;

import com.gitgle.dao.User;
import com.gitgle.response.GithubUser;

import java.time.LocalDateTime;

public class GithubUserConvert {

    public static GithubUser convert(User user){
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

    public static User convert2User(GithubUser githubUser){
        User user = new User();
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
        return user;
    }
}
