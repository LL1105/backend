package com.gitgle.service;

import com.gitgle.response.GithubReposResponse;
import com.gitgle.result.RpcResult;

public interface GithubProjectService {

    RpcResult<GithubReposResponse> getProjectByDeveloperId(String developerId);
}
