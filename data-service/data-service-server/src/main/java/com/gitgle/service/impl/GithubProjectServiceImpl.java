package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.dao.RepoContent;
import com.gitgle.request.GithubRequest;
import com.gitgle.response.*;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubProjectService;
import com.gitgle.service.RepoContentService;
import com.gitgle.service.ReposService;
import com.gitgle.utils.GithubApiRequestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@DubboService
@Slf4j
public class GithubProjectServiceImpl implements GithubProjectService {

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Resource
    private ReposService reposService;

    @Resource
    private RepoContentService repoContentService;

    @Override
    public RpcResult<GithubRepos> getRepoByOwnerAndRepoName(String developerId, String repoName) {
        RpcResult<GithubRepos> githubReposRpcResult = new RpcResult<>();
        try {
            // 先查库
            GithubRepos githubRepos = reposService.readRepos2GithubRepos(developerId, repoName);
            if(ObjectUtils.isNotEmpty(githubRepos)){
                githubReposRpcResult.setCode(RpcResultCode.SUCCESS);
                githubReposRpcResult.setData(githubRepos);
                return githubReposRpcResult;
            }
            Response response = githubApiRequestUtils.getOneRepo(developerId, repoName);
            if(!response.isSuccessful()){
                githubReposRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
                return githubReposRpcResult;
            }
            JSONObject responseBody = JSON.parseObject(response.body().string());
            log.info("Github GetRepo Response: {}", responseBody);
            githubRepos = new GithubRepos();
            githubRepos.setId(responseBody.getInteger("id"));
            githubRepos.setRepoName(responseBody.getString("name"));
            githubRepos.setOwnerLogin(responseBody.getJSONObject("owner").getString("login"));
            githubRepos.setDescription(responseBody.getString("description"));
            githubRepos.setForksCount(responseBody.getInteger("forks_count"));
            githubRepos.setStarsCount(responseBody.getInteger("stargazers_count"));
            githubRepos.setWatchersCount(responseBody.getInteger("watchers_count"));
            githubRepos.setIssueCount(responseBody.getInteger("open_issues_count"));
            githubRepos.setCreatedAt(responseBody.getString("created_at"));
            githubRepos.setUpdateAt(responseBody.getString("updated_at"));
            githubRepos.setOrPrivate(responseBody.getBoolean("private"));
            // 异步写库
            final GithubRepos finalGithubRepos = githubRepos;
            CompletableFuture.runAsync(()->{
                reposService.writeGithubRepos2Repos(finalGithubRepos);
            }).exceptionally(ex -> {
                log.error("Github Write Exception: {}", ex);
                return null;
            });
            githubReposRpcResult.setData(githubRepos);
            githubReposRpcResult.setCode(RpcResultCode.SUCCESS);
            return githubReposRpcResult;
        } catch (IOException e) {
            log.error("Github getRepo Exception: {}", e);
            githubReposRpcResult.setCode(RpcResultCode.FAILED);
            return githubReposRpcResult;
        }
    }

    @Override
    public RpcResult<GithubReposContent> getRepoContentByPath(GithubRequest githubRequest) {
        RpcResult<GithubReposContent> githubReposContentRpcResult = new RpcResult<>();
        try {
            // 先查库，没有再github上搜索
            GithubReposContent githubReposContent = repoContentService.readRepoContent2GithubReposContent(githubRequest.getPath(), githubRequest.getRepoName(), githubRequest.getOwner());
            if(ObjectUtils.isNotEmpty(githubReposContent)){
                githubReposContentRpcResult.setCode(RpcResultCode.SUCCESS);
                githubReposContentRpcResult.setData(githubReposContent);
                return githubReposContentRpcResult;
            }
            githubReposContent = new GithubReposContent();
            Response response = githubApiRequestUtils.getRepoContent(githubRequest.getOwner(), githubRequest.getRepoName(), githubRequest.getPath());
            if(!response.isSuccessful()){
                githubReposContentRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
                return githubReposContentRpcResult;
            }
            JSONObject responseBody = JSON.parseObject(response.body().string());
            log.info("Github SearchUsers Response: {}", responseBody);
            githubReposContent.setPath(responseBody.getString("path"));
            githubReposContent.setName(responseBody.getString("name"));
            githubReposContent.setSha(responseBody.getString("sha"));
            githubReposContent.setType(responseBody.getString("type"));
            githubReposContent.setEncoding(responseBody.getString("encoding"));
            githubReposContent.setSize(responseBody.getInteger("size"));
            byte[] decodedBytes = Base64.decodeBase64(responseBody.getString("content"));
            githubReposContent.setRepoName(githubRequest.getRepoName());
            githubReposContent.setRepoOwner(githubRequest.getOwner());
            githubReposContent.setContent(new String(decodedBytes));
            final GithubReposContent finalGithubReposContent = githubReposContent;
            // 异步入库
            CompletableFuture.runAsync(()->{
                repoContentService.writeGithubReposContent2RepoContent(finalGithubReposContent);
            }).exceptionally(ex -> {
                log.error("Github Write Exception: {}", ex);
                return null;
            });
            githubReposContentRpcResult.setData(githubReposContent);
            githubReposContentRpcResult.setCode(RpcResultCode.SUCCESS);
            return githubReposContentRpcResult;
        } catch (IOException e) {
            log.error("Github GetRepoContent Exception: {}", e);
            githubReposContentRpcResult.setCode(RpcResultCode.FAILED);
            return githubReposContentRpcResult;
        }
    }
}
