package com.gitgle.service;

import com.gitgle.response.GithubRepoRank;
import com.gitgle.response.GithubRepos;
import com.gitgle.response.PageRepoResponse;

import java.util.List;


public interface ReposService{

    void writeGithubRepos2Repos(GithubRepos githubRepos);

    GithubRepos readRepos2GithubRepos(String owner, String repoName);

    List<GithubRepoRank> getReposOrderByStar();

    PageRepoResponse pageRepos2GithubRepos(Integer page, Integer size);

    List<GithubRepos> getReposByLogin(String owner);
}
