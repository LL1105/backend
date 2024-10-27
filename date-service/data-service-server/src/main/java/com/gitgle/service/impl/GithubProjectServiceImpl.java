package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.GithubCommit;
import com.gitgle.response.GithubCommitResponse;
import com.gitgle.response.GithubRepos;
import com.gitgle.response.GithubReposResponse;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubProjectService;
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
public class GithubProjectServiceImpl implements GithubProjectService {

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Override
    public RpcResult<GithubReposResponse> getProjectByDeveloperId(String developerId) {
        return null;
    }

    @Override
    public RpcResult<GithubRepos> getRepoByOwnerAndRepoName(String developerId, String repoName) {
        RpcResult<GithubRepos> githubReposRpcResult = new RpcResult<>();
        try {
            Response response = githubApiRequestUtils.getOneRepo(developerId, repoName);
            JSONObject responseBody = JSON.parseObject(response.body().string());
            log.info("Github SearchUsers Response: {}", responseBody);
            if(!response.isSuccessful()){
                githubReposRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
                return githubReposRpcResult;
            }
            GithubRepos githubRepos = new GithubRepos();
            githubRepos.setId(responseBody.getString("id"));
            githubRepos.setName(responseBody.getString("name"));
            githubRepos.setFullName(responseBody.getString("full_name"));
            githubRepos.setOwnerLogin(responseBody.getJSONObject("owner").getString("login"));
            githubRepos.setDescription(responseBody.getString("description"));
            githubRepos.setForksCount(responseBody.getInteger("forks_count"));
            githubRepos.setStarsCount(responseBody.getInteger("stargazers_count"));
            githubRepos.setWatchersCount(responseBody.getInteger("watchers_count"));
            githubRepos.setCreatedAt(responseBody.getString("created_at"));
            githubRepos.setUpdateAt(responseBody.getString("updated_at"));
            githubRepos.setOrPrivate(responseBody.getBoolean("private"));
            githubReposRpcResult.setData(githubRepos);
            githubReposRpcResult.setCode(RpcResultCode.SUCCESS);
            return githubReposRpcResult;
        } catch (IOException e) {
            log.error("Github SearchUsers Exception: {}", e);
            githubReposRpcResult.setCode(RpcResultCode.FAILED);
            return githubReposRpcResult;
        }
    }
}
