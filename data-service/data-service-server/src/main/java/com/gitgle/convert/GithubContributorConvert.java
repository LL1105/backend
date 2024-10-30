package com.gitgle.convert;

import com.alibaba.fastjson.JSONObject;
import com.gitgle.dao.Contributor;
import com.gitgle.response.GithubContributor;

public class GithubContributorConvert {

    public static GithubContributor convert(JSONObject item,String repoName, String repoOwner){
        GithubContributor githubContributor = new GithubContributor();
        githubContributor.setLogin(item.getString("login"));
        githubContributor.setRepoName(repoName);
        githubContributor.setRepoOwner(repoOwner);
        githubContributor.setContributions(item.getInteger("contributions"));
        githubContributor.setId(item.getInteger("id"));
        return githubContributor;
    }

    public static GithubContributor convert(Contributor contributor){
        GithubContributor githubContributor = new GithubContributor();
        githubContributor.setLogin(contributor.getLogin());
        githubContributor.setRepoName(contributor.getRepoName());
        githubContributor.setRepoOwner(contributor.getRepoOwner());
        githubContributor.setContributions(contributor.getContributions());
        githubContributor.setId(contributor.getContributorId());
        return githubContributor;
    }
}
