package com.gitgle.service.impl;

import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.GithubDataResponse;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@Slf4j
public class GithubDataServiceImpl implements GithubDataService {

    @Override
    public RpcResult<GithubDataResponse> getAllGithubData() {
        RpcResult<GithubDataResponse> rpcResult = new RpcResult<>();
        GithubDataResponse githubDataResponse = new GithubDataResponse();
        githubDataResponse.setGithubCountry(99);
        githubDataResponse.setGithubOrganizationTotal(1111);
        githubDataResponse.setGithubCommitTotal(5798844);
        githubDataResponse.setGithubUserTotal(70000000);
        githubDataResponse.setGithubRepoTotal(100000000);
        rpcResult.setData(githubDataResponse);
        rpcResult.setCode(RpcResultCode.SUCCESS);
        return rpcResult;
    }
}
