package com.gitgle.service;

import com.gitgle.response.GithubRepos;
import com.gitgle.response.GithubReposResponse;
import com.gitgle.result.RpcResult;

/**
 * 仓库相关接口
 */
public interface GithubProjectService {

    RpcResult<GithubReposResponse> getProjectByDeveloperId(String developerId);

    /**
     * 根据仓库持有者和仓库名查询仓库信息
     * @param developId 开发者login
     * @param repoName 仓库名
     * @return 仓库信息
     */
    RpcResult<GithubRepos> getRepoByOwnerAndRepoName(String developId, String repoName);

}
