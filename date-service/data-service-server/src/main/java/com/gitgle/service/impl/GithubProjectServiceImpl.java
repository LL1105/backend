package com.gitgle.service.impl;

import com.gitgle.response.GithubReposResponse;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubProjectService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class GithubProjectServiceImpl implements GithubProjectService {

    @Override
    public RpcResult<GithubReposResponse> getProjectByDeveloperId(String developerId) {
        return null;
    }
}
