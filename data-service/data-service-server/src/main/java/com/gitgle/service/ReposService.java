package com.gitgle.service;

import com.gitgle.dao.Repos;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gitgle.response.GithubRepoRank;
import com.gitgle.response.GithubRepos;
import com.gitgle.response.PageRepoResponse;

import java.util.List;

/**
* @author maojunjun
* @description 针对表【repos】的数据库操作Service
* @createDate 2024-10-29 00:55:29
*/
public interface ReposService{

    void writeGithubRepos2Repos(GithubRepos githubRepos);

    GithubRepos readRepos2GithubRepos(String owner, String repoName);

    List<GithubRepoRank> getReposOrderByStar();

    PageRepoResponse pageRepos2GithubRepos(Integer page, Integer size);

    GithubRepos getRepoByRepoId(Integer repoId);

    List<GithubRepos> getReposByLogin(String owner);
}
