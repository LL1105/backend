package com.gitgle.service;

import com.gitgle.response.GithubCommit;
import com.gitgle.response.GithubCommitResponse;
import com.gitgle.result.RpcResult;

/**
 * Github Commit 相关接口
 */
public interface GithubCommitService {

    RpcResult<GithubCommitResponse> searchCommitsByDeveloperId(String developerId);
}
