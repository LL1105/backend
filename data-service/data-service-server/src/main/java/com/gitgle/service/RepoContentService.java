package com.gitgle.service;

import com.gitgle.dao.RepoContent;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gitgle.response.GithubReposContent;

/**
* @author maojunjun
* @description 针对表【repo_content】的数据库操作Service
* @createDate 2024-10-29 01:00:43
*/
public interface RepoContentService {

    void writeGithubReposContent2RepoContent(GithubReposContent githubReposContent);

    GithubReposContent readRepoContent2GithubReposContent(String path, String repoName, String ownerLogin);
}
