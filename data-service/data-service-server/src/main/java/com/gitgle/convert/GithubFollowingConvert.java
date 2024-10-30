package com.gitgle.convert;

import com.alibaba.fastjson.JSONObject;
import com.gitgle.response.GithubFollowing;

public class GithubFollowingConvert {

    public static GithubFollowing convert(JSONObject item){
        GithubFollowing githubFollowing = new GithubFollowing();
        githubFollowing.setId(item.getInteger("id"));
        githubFollowing.setLogin(item.getString("login"));
        githubFollowing.setAvatarUrl(item.getString("avatar_url"));
        return githubFollowing;
    }
}
