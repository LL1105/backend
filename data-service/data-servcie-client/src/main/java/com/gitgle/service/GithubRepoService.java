package com.gitgle.service;

import com.gitgle.request.GithubRequest;
import com.gitgle.response.GithubRepos;
import com.gitgle.response.GithubReposContent;
import com.gitgle.response.GithubReposResponse;
import com.gitgle.result.RpcResult;

/**
 * 仓库相关接口
 */
public interface GithubRepoService {

    /**
     * 根据仓库持有者和仓库名查询仓库信息
     * @param developId 开发者login
     * @param repoName 仓库名
     * @return 仓库信息
     */
    RpcResult<GithubRepos> getRepoByOwnerAndRepoName(String developId, String repoName);

    /**
     * 根据路径查询仓库文件内容
     * @param githubRequest 文件路径
     * @return 文件内容
     */
    RpcResult<GithubReposContent> getRepoContentByPath(GithubRequest githubRequest);

    /**
     * 列出用户的仓库
     * @param owner 用户login
     * @return 仓库列表
     */
    RpcResult<GithubReposResponse> listUserRepos(String owner);
}
