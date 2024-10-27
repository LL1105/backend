package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.dao.Commit;
import com.gitgle.mapper.CommitMapper;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@DubboService
@Slf4j
public class GithubCommitServiceImpl implements GithubCommitService {

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Resource
    private CommitMapper commitMapper;

    @Override
    public RpcResult<GithubCommitResponse> searchCommitsByDeveloperId(String developerId) {
        RpcResult<GithubCommitResponse> githubCommitResponseRpcResult = new RpcResult<>();
        GithubCommitResponse githubCommitResponse = new GithubCommitResponse();
        try {
            // 先读数据库，如果没有再请求github
            List<GithubCommit> githubCommitList = readGithubCommit(developerId);
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
                JSONObject responseBody = JSON.parseObject(response.body().string());
                log.info("Github SearchUsers Response: {}", responseBody);
                if(!response.isSuccessful()){
                    if(page.equals(1)){
                        githubCommitResponseRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
                        return githubCommitResponseRpcResult;
                    }else{
                        log.error("Github Api Failed In page:{}", page);
                        break;
                    }
                }
                for(int i=0; i<responseBody.getJSONArray("items").size(); i++){
                    JSONObject item = responseBody.getJSONArray("items").getJSONObject(i);
                    GithubCommit githubCommit = new GithubCommit();
                    githubCommit.setAuthorLogin(item.getJSONObject("author").getString("login"));
                    githubCommit.setReposId(item.getJSONObject("repository").getString("id"));
                    githubCommit.setReposName(item.getJSONObject("repository").getString("name"));
                    githubCommit.setCommitDataTime(item.getJSONObject("commit").getJSONObject("committer").getString("date"));
                    githubCommit.setReposOwner(item.getJSONObject("repository").getJSONObject("owner").getString("login"));
                    githubCommit.setSha(item.getJSONObject("commit").getJSONObject("tree").getString("sha"));
                    githubCommitList.add(githubCommit);
                    // 异步写库
                    CompletableFuture.runAsync(()-> {
                        writeGithubCommit(githubCommit);
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


    public List<GithubCommit> readGithubCommit(String authorLogin){
        List<Commit> commitList = commitMapper.selectList(Wrappers.lambdaQuery(Commit.class).eq(Commit::getAuthorLogin, authorLogin));
        return commitList.stream().map(commit -> {
            GithubCommit githubCommit = new GithubCommit();
            githubCommit.setSha(commit.getSha());
            githubCommit.setReposId(commit.getReposId());
            githubCommit.setReposName(commit.getReposName());
            githubCommit.setReposOwner(commit.getReposOwner());
            githubCommit.setCommitDataTime(commit.getCommitDateTime().toString());
            return githubCommit;
        }).collect(Collectors.toList());
    }

    public void writeGithubCommit(GithubCommit githubCommit){
        // 先根据sha查询数据库中是否存在
        Commit commit = commitMapper.selectOne(Wrappers.lambdaQuery(Commit.class).eq(Commit::getSha, githubCommit.getSha()));
        if(ObjectUtils.isNotEmpty(commit)){
            return;
        }
        // 如果没有则入库
        commit = new Commit();
        commit.setCommitDateTime(githubApiRequestUtils.parseTime(githubCommit.getCommitDataTime()));
        commit.setAuthorLogin(githubCommit.getAuthorLogin());
        commit.setReposId(githubCommit.getReposId());
        commit.setReposName(githubCommit.getReposName());
        commit.setReposOwner(githubCommit.getReposOwner());
        commit.setSha(githubCommit.getSha());
        commit.setCreateTime(LocalDateTime.now());
        commit.setUpdateTime(LocalDateTime.now());
        commitMapper.insert(commit);
    }
}
