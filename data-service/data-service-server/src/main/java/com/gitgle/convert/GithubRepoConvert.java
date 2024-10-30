package com.gitgle.convert;

import com.alibaba.fastjson.JSONObject;
import com.gitgle.dao.Repos;
import com.gitgle.response.GithubRepos;

public class GithubRepoConvert {

    public static GithubRepos convert(JSONObject responseBody){
        GithubRepos githubRepos = new GithubRepos();
        githubRepos.setId(responseBody.getInteger("id"));
        githubRepos.setRepoName(responseBody.getString("name"));
        githubRepos.setOwnerLogin(responseBody.getJSONObject("owner").getString("login"));
        githubRepos.setDescription(responseBody.getString("description"));
        githubRepos.setForksCount(responseBody.getInteger("forks_count"));
        githubRepos.setStarsCount(responseBody.getInteger("stargazers_count"));
        githubRepos.setWatchersCount(responseBody.getInteger("watchers_count"));
        githubRepos.setIssueCount(responseBody.getInteger("open_issues_count"));
        githubRepos.setCreatedAt(responseBody.getString("created_at"));
        githubRepos.setUpdateAt(responseBody.getString("updated_at"));
        githubRepos.setOrPrivate(responseBody.getBoolean("private"));
        return githubRepos;
    }

    public static GithubRepos convert(Repos repo){
        GithubRepos githubRepos = new GithubRepos();
        githubRepos.setWatchersCount(repo.getWatchersCount());
        githubRepos.setOwnerLogin(repo.getOwnerlogin());
        githubRepos.setId(repo.getId());
        githubRepos.setRepoName(repo.getRepoName());
        githubRepos.setOrPrivate(repo.getOrPrivate());
        githubRepos.setCreatedAt(repo.getCreateAt());
        githubRepos.setUpdateAt(repo.getUpdateAt());
        githubRepos.setStarsCount(repo.getStarsCount());
        githubRepos.setForksCount(repo.getForksCount());
        githubRepos.setIssueCount(repo.getIssueCount());
        githubRepos.setDescription(repo.getDescription());
        return githubRepos;
    }
}
