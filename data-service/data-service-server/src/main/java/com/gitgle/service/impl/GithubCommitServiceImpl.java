package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.convert.GithubCommitConvert;
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
                setResponse(githubCommitResponse, githubCommitResponseRpcResult, githubCommitList, RpcResultCode.SUCCESS);
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
                        setResponse(githubCommitResponse, githubCommitResponseRpcResult, null, RpcResultCode.Github_RESPONSE_FAILED);
                        return githubCommitResponseRpcResult;
                    }else{
                        log.error("Github Api 根据login查询commit失败，page:{}", page);
                        break;
                    }
                }
                JSONObject responseBody = JSON.parseObject(response.body().string());
                for(int i=0; i<responseBody.getJSONArray("items").size(); i++){
                    JSONObject item = responseBody.getJSONArray("items").getJSONObject(i);
                    GithubCommit githubCommit = GithubCommitConvert.convert(item);
                    githubCommitList.add(githubCommit);
                    // 异步写库
                    CompletableFuture.runAsync(()-> {
                        commitService.writeGithubCommit2Commit(githubCommit);
                    });
                }
                if(responseBody.getInteger("total_count")<page*100){
                    break;
                }
                page++;
            }
            setResponse(githubCommitResponse, githubCommitResponseRpcResult, githubCommitList, RpcResultCode.SUCCESS);
            return githubCommitResponseRpcResult;
        } catch (IOException e) {
            log.error("根据login搜索Commit失败： {}", e.getMessage());
            setResponse(githubCommitResponse, githubCommitResponseRpcResult, null, RpcResultCode.FAILED);
            return githubCommitResponseRpcResult;
        }
    }

    private void setResponse(GithubCommitResponse githubCommitResponse, RpcResult<GithubCommitResponse> githubCommitResponseRpcResult,
                                    List<GithubCommit> githubCommitList, RpcResultCode rpcResultCode){
        githubCommitResponse.setGithubCommitList(githubCommitList);
        githubCommitResponseRpcResult.setCode(rpcResultCode);
        githubCommitResponseRpcResult.setData(githubCommitResponse);
    }
}
