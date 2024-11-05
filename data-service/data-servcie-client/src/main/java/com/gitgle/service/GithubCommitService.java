package com.gitgle.service;

import com.gitgle.request.GithubRequest;
import com.gitgle.response.GithubCommit;
import com.gitgle.response.GithubCommitResponse;
import com.gitgle.result.RpcResult;

/**
 * Github Commit 相关接口
 */
public interface GithubCommitService {

    /**
     * 根据开发者login查询commit列表
     * @param developerId 开发者login
     * @return commit列表
     */
    RpcResult<GithubCommitResponse> searchCommitsByDeveloperId(String developerId);
}
