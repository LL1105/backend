package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.request.GithubRequest;
import com.gitgle.response.GithubCommit;
import com.gitgle.response.GithubCommitResponse;
import com.gitgle.result.RpcResult;
import com.gitgle.service.CommitService;
import com.gitgle.service.GithubCommitService;
import com.gitgle.utils.GithubApiRequestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@DubboService
@Slf4j
public class GithubCommitServiceImpl implements GithubCommitService {

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Resource
    private CommitService commitService;

    @Override
    public RpcResult<GithubCommitResponse> searchCommitsByDeveloperId(String developerId) {
        RpcResult<GithubCommitResponse> githubCommitResponseRpcResult = new RpcResult<>();
        GithubCommitResponse githubCommitResponse = new GithubCommitResponse();
        try {
            // 先读数据库，如果没有再请求github
            List<GithubCommit> githubCommitList = commitService.readCommit2GithubCommit(developerId);
            if(ObjectUtils.isNotEmpty(githubCommitList)){
                githubCommitResponse.setGithubCommitList(githubCommitList);
                githubCommitResponseRpcResult.setCode(RpcResultCode.SUCCESS);
                githubCommitResponseRpcResult.setData(githubCommitResponse);
                return githubCommitResponseRpcResult;
            }
            HashMap<String, String> searchParams = new HashMap<>();
            searchParams.put("q", "author:"+developerId);
            searchParams.put("per_page", "100");
            // 循环抓取直到抓完所有
            githubCommitList = new ArrayList<>();
            Integer page = 1;
            while(true){
                searchParams.put("page", page.toString());
                Response response = githubApiRequestUtils.searchCommits(searchParams);
                if(!response.isSuccessful()){
                    if(page.equals(1)){
                        githubCommitResponseRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
                        return githubCommitResponseRpcResult;
                    }else{
                        log.error("Github Api Failed In page:{}", page);
                        break;
                    }
                }
                JSONObject responseBody = JSON.parseObject(response.body().string());
                log.info("Github SearchUsers Response: {}", responseBody);
                for(int i=0; i<responseBody.getJSONArray("items").size(); i++){
                    JSONObject item = responseBody.getJSONArray("items").getJSONObject(i);
                    GithubCommit githubCommit = json2GithubCommit(item);
                    githubCommitList.add(githubCommit);
                    // 异步写库
                    CompletableFuture.runAsync(()-> {
                        commitService.writeGithubCommit2Commit(githubCommit);
                    }).exceptionally(ex -> {
                        log.error("Github Commit Write Exception: {}", ex);
                        return null;
                    });
                }
                if(responseBody.getInteger("total_count")<page*100){
                    break;
                }
                page++;
            }
            githubCommitResponse.setGithubCommitList(githubCommitList);
            githubCommitResponseRpcResult.setData(githubCommitResponse);
            githubCommitResponseRpcResult.setCode(RpcResultCode.SUCCESS);
            return githubCommitResponseRpcResult;
        } catch (IOException e) {
            log.error("Github SearchUsers Exception: {}", e);
            githubCommitResponseRpcResult.setCode(RpcResultCode.FAILED);
            return githubCommitResponseRpcResult;
        }
    }

    @Override
    public RpcResult<GithubCommitResponse> listCommitsByRepoAndAuthor(GithubRequest githubRequest) {
        RpcResult<GithubCommitResponse> githubCommitResponseRpcResult = new RpcResult<>();
        GithubCommitResponse githubCommitResponse = new GithubCommitResponse();
        try {
            // 先读数据库，如果没有再请求github
            List<GithubCommit> githubCommitList = commitService.readCommit2GithubCommit(githubRequest);
            if(ObjectUtils.isNotEmpty(githubCommitList)){
                githubCommitResponse.setGithubCommitList(githubCommitList);
                githubCommitResponseRpcResult.setCode(RpcResultCode.SUCCESS);
                githubCommitResponseRpcResult.setData(githubCommitResponse);
                return githubCommitResponseRpcResult;
            }
            HashMap<String, String> queryParams = new HashMap<>();
            githubCommitList= new ArrayList<>();
            queryParams.put("per_page", "100");
            queryParams.put("author", githubRequest.getAuthor());
            Integer page = 1;
            while(true){
                queryParams.put("page", page.toString());
                Response response = githubApiRequestUtils.listCommit(githubRequest.getOwner(), githubRequest.getRepoName(), queryParams);
                if(!response.isSuccessful()){
                    if(page.equals(1)){
                        githubCommitResponseRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
                        return githubCommitResponseRpcResult;
                    }else{
                        log.error("Github Api Failed In page:{}", page);
                        break;
                    }
                }
                JSONArray responseBody = JSON.parseArray(response.body().string());
                log.info("Github List Commits Response: {}", responseBody);
                for(int i=0; i<responseBody.size(); i++){
                    JSONObject item =responseBody.getJSONObject(i);
                    GithubCommit githubCommit = new GithubCommit();
                    githubCommit.setSha(item.getString("sha"));
                    githubCommit.setAuthorLogin(item.getJSONObject("author").getString("login"));
                    githubCommit.setCommitDataTime(item.getJSONObject("commit").getJSONObject("committer").getString("date"));
                    githubCommitList.add(githubCommit);
                    // 异步写库
                    CompletableFuture.runAsync(()-> {
                        commitService.writeGithubCommit2Commit(githubCommit);
                    }).exceptionally(ex -> {
                        log.error("Github Commit Write Exception: {}", ex);
                        return null;
                    });
                }
                if(responseBody.size() < 100){
                    break;
                }
                page++;
            }
            githubCommitResponse.setGithubCommitList(githubCommitList);
            githubCommitResponseRpcResult.setCode(RpcResultCode.SUCCESS);
            githubCommitResponseRpcResult.setData(githubCommitResponse);
            return githubCommitResponseRpcResult;
        } catch (IOException e) {
            log.info("Github List Commits Exception: {}", e);
            githubCommitResponseRpcResult.setCode(RpcResultCode.FAILED);
            return githubCommitResponseRpcResult;
        }
    }

    public GithubCommit json2GithubCommit(JSONObject jsonObject){
        GithubCommit githubCommit = new GithubCommit();
        githubCommit.setAuthorLogin(jsonObject.getJSONObject("author").getString("login"));
        githubCommit.setReposId(jsonObject.getJSONObject("repository").getString("id"));
        githubCommit.setReposName(jsonObject.getJSONObject("repository").getString("name"));
        githubCommit.setCommitDataTime(jsonObject.getJSONObject("commit").getJSONObject("committer").getString("date"));
        githubCommit.setReposOwner(jsonObject.getJSONObject("repository").getJSONObject("owner").getString("login"));
        githubCommit.setSha(jsonObject.getJSONObject("commit").getJSONObject("tree").getString("sha"));
        return githubCommit;
    }
}
