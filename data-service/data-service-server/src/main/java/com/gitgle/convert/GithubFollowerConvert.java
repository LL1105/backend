package com.gitgle.convert;

import com.alibaba.fastjson.JSONObject;
import com.gitgle.response.GithubFollowers;

public class GithubFollowerConvert {

    public static GithubFollowers convert(JSONObject item){
        GithubFollowers githubFollowers = new GithubFollowers();
        githubFollowers.setId(item.getInteger("id"));
        githubFollowers.setLogin(item.getString("login"));
        githubFollowers.setAvatarUrl(item.getString("avatar_url"));
        return githubFollowers;
    }
}
