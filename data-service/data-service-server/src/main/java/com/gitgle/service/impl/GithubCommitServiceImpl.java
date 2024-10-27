package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.GithubCommit;
import com.gitgle.response.GithubCommitResponse;
import com.gitgle.result.RpcResult;
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

@DubboService
@Slf4j
public class GithubCommitServiceImpl implements GithubCommitService {

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Override
    public RpcResult<GithubCommitResponse> searchCommitsByDeveloperId(String developerId) {
        RpcResult<GithubCommitResponse> githubCommitResponseRpcResult = new RpcResult<>();
        try {
            HashMap<String, String> searchParams = new HashMap<>();
            searchParams.put("q", "author:"+developerId);
            Response response = githubApiRequestUtils.searchCommits(searchParams);
            JSONObject responseBody = JSON.parseObject(response.body().string());
            log.info("Github SearchUsers Response: {}", responseBody);
            if(!response.isSuccessful()){
                githubCommitResponseRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
                return githubCommitResponseRpcResult;
            }
            List<GithubCommit> githubCommitList = new ArrayList<>();
            for(int i=0; i<responseBody.getJSONArray("items").size(); i++){
                JSONObject item = responseBody.getJSONArray("items").getJSONObject(i);
                GithubCommit githubCommit = new GithubCommit();
                githubCommit.setAuthorLogin(item.getJSONObject("author").getString("login"));
                githubCommit.setReposId(item.getJSONObject("repository").getString("id"));
                githubCommit.setReposName(item.getJSONObject("repository").getString("name"));
                githubCommit.setCommitDataTime(item.getJSONObject("commit").getJSONObject("committer").getString("date"));
                githubCommit.setReposOwner(item.getJSONObject("repository").getJSONObject("owner").getString("login"));
                githubCommitList.add(githubCommit);
            }
            GithubCommitResponse githubCommitResponse = new GithubCommitResponse();
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
}
