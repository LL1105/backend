package com.gitgle.service;


import com.gitgle.response.GithubDataResponse;
import com.gitgle.result.RpcResult;

public interface GithubDataService {

    /**
     * 获取所有Github数据统计
     */
    RpcResult<GithubDataResponse> getAllGithubData();

}
