package com.gitgle.convert;

import com.alibaba.fastjson.JSONObject;
import com.gitgle.dao.Follower;
import com.gitgle.response.GithubFollowers;

import java.time.LocalDateTime;

public class GithubFollowerConvert {

    public static GithubFollowers convert(JSONObject item){
        GithubFollowers githubFollowers = new GithubFollowers();
        githubFollowers.setId(item.getInteger("id"));
        githubFollowers.setLogin(item.getString("login"));
        githubFollowers.setAvatarUrl(item.getString("avatar_url"));
        return githubFollowers;
    }

    public static Follower convert2Follower(GithubFollowers githubFollowers, String login){
        Follower follower = new Follower();
        follower.setFollowerLogin(githubFollowers.getLogin());
        follower.setFollowingLogin(login);
        follower.setFollowerAvatarUrl(githubFollowers.getAvatarUrl());
        follower.setFollowerId(githubFollowers.getId());
        follower.setCreateTime(LocalDateTime.now());
        follower.setUpdateTime(LocalDateTime.now());
        return follower;
    }
}
