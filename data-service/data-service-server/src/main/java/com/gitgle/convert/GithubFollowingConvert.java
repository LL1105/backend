package com.gitgle.convert;

import com.alibaba.fastjson.JSONObject;
import com.gitgle.dao.Follower;
import com.gitgle.response.GithubFollowing;

import java.time.LocalDateTime;

public class GithubFollowingConvert {

    public static GithubFollowing convert(JSONObject item){
        GithubFollowing githubFollowing = new GithubFollowing();
        githubFollowing.setLogin(item.getString("login"));
        return githubFollowing;
    }

    public static Follower convert2Following(GithubFollowing githubFollowing, String login){
        Follower follower = new Follower();
        follower.setFollowingLogin(githubFollowing.getLogin());
        follower.setFollowerLogin(login);
        follower.setCreateTime(LocalDateTime.now());
        follower.setUpdateTime(LocalDateTime.now());
        return follower;
    }
}
