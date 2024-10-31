package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gitgle.constant.RedisConstant;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.GithubDataResponse;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubDataService;
import com.gitgle.utils.GithubApiRequestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@DubboService
@Slf4j
public class GithubDataServiceImpl implements GithubDataService {

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Resource
    private RedisTemplate<String, Integer> redisTemplate;

    @Override
    public RpcResult<GithubDataResponse> getAllGithubData() {
        RpcResult<GithubDataResponse> rpcResult = new RpcResult<>();
        GithubDataResponse githubDataResponse = new GithubDataResponse();
        try {
            CompletableFuture<Integer> githubCodeTotal = getGithubCodeTotal();
            CompletableFuture<Integer> githubCommitTotal = getGithubCommitTotal();
            CompletableFuture<Integer> githubUserTotal = getGithubUserTotal();
            CompletableFuture<Integer> githubRepoTotal = getGithubRepoTotal();
            CompletableFuture.allOf(githubCodeTotal, githubCommitTotal, githubUserTotal, githubRepoTotal).join();
            githubDataResponse.setGithubCodeTotal(githubCodeTotal.get());
            githubDataResponse.setGithubCommitTotal(githubCommitTotal.get());
            githubDataResponse.setGithubUserTotal(githubUserTotal.get());
            githubDataResponse.setGithubRepoTotal(githubRepoTotal.get());
            rpcResult.setData(githubDataResponse);
            rpcResult.setCode(RpcResultCode.SUCCESS);
            return rpcResult;
        }catch (Exception e){
            rpcResult.setCode(RpcResultCode.FAILED);
            return rpcResult;
        }
    }

    public CompletableFuture<Integer> getGithubCommitTotal() throws IOException {
        return CompletableFuture.supplyAsync(()->{
            try{
                Integer githubCommitTatal = redisTemplate.opsForValue().get(RedisConstant.GITHUB_COMMIT_TOTAL);
                if(Objects.nonNull(githubCommitTatal)){
                    return githubCommitTatal;
                }
                Map<String, String> params = new HashMap<>();
                params.put("q", "author-date:>2000-01-01");
                params.put("page", "1");
                Response response = githubApiRequestUtils.searchCommits(params);
                if (!response.isSuccessful()) {
                    throw new IOException("Github SearchCommit Exception: " + response.body().string());
                }
                JSONObject responseBody = JSON.parseObject(response.body().string());
                log.info("public:{}", responseBody.getInteger("total_count"));
                githubCommitTatal = Math.abs(responseBody.getInteger("total_count"));
                redisTemplate.opsForValue().set(RedisConstant.GITHUB_COMMIT_TOTAL, githubCommitTatal, 1, TimeUnit.DAYS);
                return githubCommitTatal;
            }catch (Exception e){
                log.error("GetGithubCommitTotal Error{}", e.getMessage());
                return 0;
            }
        });
    }

    public CompletableFuture<Integer> getGithubUserTotal() throws IOException {
        return CompletableFuture.supplyAsync(()->{
            try{
                Integer githubUserTotal = redisTemplate.opsForValue().get(RedisConstant.GITHUB_USER_TOTAL);
                if(Objects.nonNull(githubUserTotal)){
                    return githubUserTotal;
                }
                githubUserTotal = githubApiRequestUtils.getGithubUserTotal();
                redisTemplate.opsForValue().set(RedisConstant.GITHUB_USER_TOTAL, githubUserTotal, 1, TimeUnit.DAYS);
                return githubUserTotal;
            }catch (Exception e){
                log.error("GetGithubUserTotal Error: {}", e.getMessage());
                return 0;
            }
        });
    }

    public CompletableFuture<Integer> getGithubCodeTotal() throws IOException {
        return CompletableFuture.supplyAsync(()->{
            try{
                Integer githubCodeTotal = redisTemplate.opsForValue().get(RedisConstant.GITHUB_CODE_TOTAL);
                if(Objects.nonNull(githubCodeTotal)){
                    return githubCodeTotal;
                }
                Map<String, String> params = new HashMap<>();
                params.put("q", "size:>=0");
                params.put("page", "1");
                Response response = githubApiRequestUtils.searchCode(params);
                if (!response.isSuccessful()) {
                    throw new IOException("Github SearchCode Exception: " + response.body().string());
                }
                JSONObject responseBody = JSON.parseObject(response.body().string());
                githubCodeTotal = responseBody.getInteger("total_count");
                redisTemplate.opsForValue().set(RedisConstant.GITHUB_CODE_TOTAL, githubCodeTotal, 1, TimeUnit.DAYS);
                return githubCodeTotal;
            }catch (Exception e){
                log.error("GetGithubCodeTotal Error: {}", e.getMessage());
                return 0;
            }
        });
    }

    public CompletableFuture<Integer> getGithubRepoTotal() throws IOException {
        return CompletableFuture.supplyAsync(()->{
            try{
                Integer githubRepoTotal = redisTemplate.opsForValue().get(RedisConstant.GITHUB_REPO_TOTAL);
                if(Objects.nonNull(githubRepoTotal)){
                    return githubRepoTotal;
                }
                Map<String, String> params = new HashMap<>();
                params.put("q", "size:>=0");
                params.put("page", "1");
                Response response = githubApiRequestUtils.searchRepos(params);
                if (!response.isSuccessful()) {
                    throw new IOException("Github SearchRepo Exception: " + response.body().string());
                }
                JSONObject responseBody = JSON.parseObject(response.body().string());
                githubRepoTotal = responseBody.getInteger("total_count");
                redisTemplate.opsForValue().set(RedisConstant.GITHUB_REPO_TOTAL, githubRepoTotal, 1, TimeUnit.DAYS);
                return githubRepoTotal;
            }catch (Exception e){
                log.error("GetGithubRepoTotal Error: {}", e.getMessage());
                return 0;
            }
        });
    }
}
