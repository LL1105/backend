package com.gitgle.service;


import com.gitgle.response.GithubDataResponse;
import com.gitgle.result.RpcResult;

public interface GithubDataService {

    RpcResult<GithubDataResponse> getAllGithubData();

}
