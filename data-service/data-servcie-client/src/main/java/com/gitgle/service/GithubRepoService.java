package com.gitgle.service;

import com.gitgle.request.GithubRequest;
import com.gitgle.response.*;
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

    /**
     * 列出仓库贡献者
     */
    RpcResult<GithubContributorResponse> listRepoContributors(String owner, String repoName);

    /**
     * 获取仓库语言
     */
    RpcResult<GithubLanguagesResponse> getRepoLanguages(String owner, String repoName);

    /**
     * 获取热门仓库
     */
    RpcResult<GithubRepoRankResponse> getHotRepos();

    /**
     * 分页获取排序后的仓库
     */
    RpcResult<PageRepoResponse> getReposOrderByStar(Integer page, Integer size);

    /**
     * 根据仓库id查询仓库详情
     */
    RpcResult<GithubRepos> getRepoById(Integer repoId);
}
