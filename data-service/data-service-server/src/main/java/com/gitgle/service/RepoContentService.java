package com.gitgle.service;

import com.gitgle.response.GithubReposContent;

public interface RepoContentService {

    void writeGithubReposContent2RepoContent(GithubReposContent githubReposContent);

    GithubReposContent readRepoContent2GithubReposContent(String path, String repoName, String ownerLogin);
}
