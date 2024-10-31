package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.GithubDataResponse;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubDataService;
import com.gitgle.utils.GithubApiRequestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@DubboService
@Slf4j
public class GithubDataServiceImpl implements GithubDataService {

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Override
    public RpcResult<GithubDataResponse> getAllGithubData() {
        RpcResult<GithubDataResponse> rpcResult = new RpcResult<>();
        GithubDataResponse githubDataResponse = new GithubDataResponse();
        try {
            githubDataResponse.setGithubCodeTotal(getGithubCodeTotal().get());
            githubDataResponse.setGithubCommitTotal(getGithubCommitTotal().get());
            githubDataResponse.setGithubUserTotal(getGithubUserTotal().get());
            githubDataResponse.setGithubRepoTotal(getGithubRepoTotal().get());
            rpcResult.setData(githubDataResponse);
            rpcResult.setCode(RpcResultCode.SUCCESS);
            return rpcResult;
        }catch (Exception e){
            rpcResult.setCode(RpcResultCode.FAILED);
            return rpcResult;
        }
    }

    public Integer getPublicGithubCommitTotal() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("q", "author-date:>2000-01-01");
        params.put("page", "1");
        Response response = githubApiRequestUtils.searchCommits(params);
        if (!response.isSuccessful()) {
            throw new IOException("Github SearchCommit Exception: " + response.body().string());
        }
        JSONObject responseBody = JSON.parseObject(response.body().string());
        log.info("public:{}", responseBody.getInteger("total_count"));
        return Math.abs(responseBody.getInteger("total_count"));
    }

    public CompletableFuture<Integer> getGithubCommitTotal() throws IOException {
        return CompletableFuture.supplyAsync(()->{
            try{
                return getPublicGithubCommitTotal();
            }catch (Exception e){
                log.error("GetGithubCommitTotal Error{}", e.getMessage());
                return 0;
            }
        });
    }

    public CompletableFuture<Integer> getGithubUserTotal() throws IOException {
        return CompletableFuture.supplyAsync(()->{
            try{
                return githubApiRequestUtils.getGithubUserTotal();
            }catch (Exception e){
                log.error("GetGithubUserTotal Error: {}", e.getMessage());
                return 0;
            }
        });
    }

    public CompletableFuture<Integer> getGithubCodeTotal() throws IOException {
        return CompletableFuture.supplyAsync(()->{
            try{
                Map<String, String> params = new HashMap<>();
                params.put("q", "size:>=0");
                params.put("page", "1");
                Response response = githubApiRequestUtils.searchCode(params);
                if (!response.isSuccessful()) {
                    throw new IOException("Github SearchCode Exception: " + response.body().string());
                }
                JSONObject responseBody = JSON.parseObject(response.body().string());
                return responseBody.getInteger("total_count");
            }catch (Exception e){
                log.error("GetGithubCodeTotal Error: {}", e.getMessage());
                return 0;
            }
        });
    }

    public CompletableFuture<Integer> getGithubRepoTotal() throws IOException {
        return CompletableFuture.supplyAsync(()->{
            try{
                Map<String, String> params = new HashMap<>();
                params.put("q", "size:>=0");
                params.put("page", "1");
                Response response = githubApiRequestUtils.searchRepos(params);
                if (!response.isSuccessful()) {
                    throw new IOException("Github SearchRepo Exception: " + response.body().string());
                }
                JSONObject responseBody = JSON.parseObject(response.body().string());
                return responseBody.getInteger("total_count");
            }catch (Exception e){
                log.error("GetGithubRepoTotal Error: {}", e.getMessage());
                return 0;
            }
        });
    }
}
