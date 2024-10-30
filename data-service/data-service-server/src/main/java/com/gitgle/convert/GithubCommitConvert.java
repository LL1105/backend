package com.gitgle.convert;

import com.alibaba.fastjson.JSONObject;
import com.gitgle.response.GithubCommit;

public class GithubCommitConvert {

    public static GithubCommit convert(JSONObject jsonObject){
        GithubCommit githubCommit = new GithubCommit();
        githubCommit.setAuthorLogin(jsonObject.getJSONObject("author").getString("login"));
        githubCommit.setReposId(jsonObject.getJSONObject("repository").getString("id"));
        githubCommit.setReposName(jsonObject.getJSONObject("repository").getString("name"));
        githubCommit.setCommitDataTime(jsonObject.getJSONObject("commit").getJSONObject("committer").getString("date"));
        githubCommit.setReposOwner(jsonObject.getJSONObject("repository").getJSONObject("owner").getString("login"));
        githubCommit.setSha(jsonObject.getJSONObject("commit").getJSONObject("tree").getString("sha"));
        return githubCommit;
    }
}
