package com.gitgle.convert;

import com.alibaba.fastjson.JSONObject;
import com.gitgle.dao.Repos;
import com.gitgle.response.GithubRepoRank;
import com.gitgle.response.GithubRepos;

import java.time.LocalDateTime;

public class GithubRepoConvert {

    public static GithubRepoRank convert2Rank(Repos repos){
        GithubRepoRank githubRepoRank = new GithubRepoRank();
        githubRepoRank.setRepoName(repos.getRepoName());
        githubRepoRank.setOwnerLogin(repos.getOwnerlogin());
        githubRepoRank.setOwnerAvatarUrl(repos.getOwnerAvatarUrl());
        githubRepoRank.setStarsCount(repos.getStarsCount());
        githubRepoRank.setForksCount(repos.getForksCount());
        githubRepoRank.setWatchersCount(repos.getWatchersCount());
        githubRepoRank.setRepoId(repos.getRepoId());
        return githubRepoRank;
    }

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
        githubRepos.setUrl(responseBody.getString("html_url"));
        githubRepos.setOwnerAvatarUrl(responseBody.getJSONObject("owner").getString("avatar_url"));
        return githubRepos;
    }

    public static GithubRepos convert(Repos repo){
        GithubRepos githubRepos = new GithubRepos();
        githubRepos.setWatchersCount(repo.getWatchersCount());
        githubRepos.setOwnerLogin(repo.getOwnerlogin());
        githubRepos.setId(repo.getRepoId());
        githubRepos.setRepoName(repo.getRepoName());
        githubRepos.setOrPrivate(repo.getOrPrivate());
        githubRepos.setCreatedAt(repo.getCreateAt());
        githubRepos.setUpdateAt(repo.getUpdateAt());
        githubRepos.setStarsCount(repo.getStarsCount());
        githubRepos.setForksCount(repo.getForksCount());
        githubRepos.setIssueCount(repo.getIssueCount());
        githubRepos.setDescription(repo.getDescription());
        githubRepos.setUrl(repo.getUrl());
        githubRepos.setOwnerAvatarUrl(repo.getOwnerAvatarUrl());
        return githubRepos;
    }

    public static Repos convert2Repos(GithubRepos githubRepos){
        Repos repo = new Repos();
        repo.setCreateTime(LocalDateTime.now());
        repo.setUpdateTime(LocalDateTime.now());
        repo.setOwnerlogin(githubRepos.getOwnerLogin());
        repo.setWatchersCount(githubRepos.getWatchersCount());
        repo.setRepoName(githubRepos.getRepoName());
        repo.setOrPrivate(githubRepos.getOrPrivate());
        repo.setCreateAt(githubRepos.getCreatedAt());
        repo.setUpdateAt(githubRepos.getUpdateAt());
        repo.setStarsCount(githubRepos.getStarsCount());
        repo.setForksCount(githubRepos.getForksCount());
        repo.setIssueCount(githubRepos.getIssueCount());
        repo.setDescription(githubRepos.getDescription());
        repo.setRepoId(githubRepos.getId());
        repo.setUrl(githubRepos.getUrl());
        repo.setOwnerAvatarUrl(githubRepos.getOwnerAvatarUrl());
        return repo;
    }
}
