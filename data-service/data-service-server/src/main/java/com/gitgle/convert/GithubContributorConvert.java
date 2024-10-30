package com.gitgle.convert;

import com.alibaba.fastjson.JSONObject;
import com.gitgle.response.GithubContributor;

public class GithubContributorConvert {

    public static GithubContributor convert(JSONObject item,String repoName, String repoOwner){
        GithubContributor githubContributor = new GithubContributor();
        githubContributor.setLogin(item.getString("login"));
        githubContributor.setRepoName(repoName);
        githubContributor.setRepoOwner(repoOwner);
        githubContributor.setContributions(item.getInteger("contributions"));
        return githubContributor;
    }
}
