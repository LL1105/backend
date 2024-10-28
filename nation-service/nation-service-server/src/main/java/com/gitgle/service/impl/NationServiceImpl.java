package com.gitgle.service.impl;

import com.gitgle.response.NationResponse;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubUserService;
import com.gitgle.service.NationService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class NationServiceImpl implements NationService {

    @DubboReference
    private GithubUserService githubUserService;

    @Override
    public RpcResult<NationResponse> getNationByDeveloperId(String login) {
        return null;
    }
}
