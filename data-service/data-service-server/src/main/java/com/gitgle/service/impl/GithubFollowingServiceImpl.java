package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.convert.GithubFollowerConvert;
import com.gitgle.convert.GithubFollowingConvert;
import com.gitgle.response.GithubFollowers;
import com.gitgle.response.GithubFollowersResponse;
import com.gitgle.response.GithubFollowing;
import com.gitgle.response.GithubFollowingResponse;
import com.gitgle.result.RpcResult;
import com.gitgle.service.FollowerService;
import com.gitgle.service.GithubFollowingService;
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
public class GithubFollowingServiceImpl implements GithubFollowingService {

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Resource
    private FollowerService followerService;

    @Override
    public RpcResult<GithubFollowersResponse> getFollowersByDeveloperId(String developerId) {
        RpcResult<GithubFollowersResponse> githubFollowersRpcResult = new RpcResult<>();
        GithubFollowersResponse githubFollowersResponse = new GithubFollowersResponse();
        try {
            // 先查库，没有再github上搜索
            List<GithubFollowers> githubFollowersList = followerService.readFollower2GithubFollowers(developerId);
            if(ObjectUtils.isNotEmpty(githubFollowersList)){
                githubFollowersResponse.setGithubFollowersList(githubFollowersList);
                githubFollowersRpcResult.setCode(RpcResultCode.SUCCESS);
                githubFollowersRpcResult.setData(githubFollowersResponse);
                return githubFollowersRpcResult;
            }
            HashMap<String, String> queryParams = new HashMap<>();
            githubFollowersList = new ArrayList<>();
            queryParams.put("per_page", "100");
            Integer page = 1;
            while(true){
                queryParams.put("page", page.toString());
                Response response = githubApiRequestUtils.getUserFollowers(developerId, queryParams);
                if(!response.isSuccessful()){
                    if(page.equals(1)){
                        githubFollowersRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
                        return githubFollowersRpcResult;
                    }else{
                        log.error("Github Api Failed In page:{}", page);
                        break;
                    }
                }
                JSONArray responseBody = JSON.parseArray(response.body().string());
                log.info("Github List Follower Response: {}", responseBody);
                for(int i=0; i<responseBody.size(); i++){
                    JSONObject item =responseBody.getJSONObject(i);
                    GithubFollowers githubFollowers = GithubFollowerConvert.convert(item);
                    githubFollowersList.add(githubFollowers);
                    // 异步写库
                    CompletableFuture.runAsync(()-> {
                        followerService.writeGithubFollower2Follower(githubFollowers, developerId);
                    }).exceptionally(ex -> {
                        log.error("Github Follower Write Exception: {}", ex);
                        return null;
                    });
                }
                if(responseBody.size() < 100){
                    break;
                }
                page++;
            }
            githubFollowersResponse.setGithubFollowersList(githubFollowersList);
            githubFollowersRpcResult.setCode(RpcResultCode.SUCCESS);
            githubFollowersRpcResult.setData(githubFollowersResponse);
            return githubFollowersRpcResult;
        } catch (IOException e) {
            log.info("Github GetFollowers Exception: {}", e);
            githubFollowersRpcResult.setCode(RpcResultCode.FAILED);
            return githubFollowersRpcResult;
        }
    }

    @Override
    public RpcResult<GithubFollowingResponse> listUserFollowingByDeveloperId(String developerId) {
        RpcResult<GithubFollowingResponse> githubFollowingRpcResult = new RpcResult<>();
        GithubFollowingResponse githubFollowingResponse = new GithubFollowingResponse();
        try {
            // 先查库，没有再github上搜索
            List<GithubFollowing> githubFollowingList = followerService.readFollowing2GithubFollowing(developerId);
            if(ObjectUtils.isNotEmpty(githubFollowingList)){
                githubFollowingResponse.setGithubFollowingList(githubFollowingList);
                githubFollowingRpcResult.setCode(RpcResultCode.SUCCESS);
                githubFollowingRpcResult.setData(githubFollowingResponse);
                return githubFollowingRpcResult;
            }
            HashMap<String, String> queryParams = new HashMap<>();
            queryParams.put("per_page", "100");
            Integer page = 1;
            githubFollowingList = new ArrayList<>();
            while(true){
                queryParams.put("page", page.toString());
                Response response = githubApiRequestUtils.listUserFollowing(developerId, queryParams);
                if(!response.isSuccessful()){
                    if(page.equals(1)){
                        githubFollowingRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
                        return githubFollowingRpcResult;
                    }else{
                        log.error("Github Api Failed In page:{}", page);
                        break;
                    }
                }
                JSONArray responseBody = JSON.parseArray(response.body().string());
                log.info("Github List Following Response: {}", responseBody);
                for(int i=0; i<responseBody.size(); i++){
                    JSONObject item =responseBody.getJSONObject(i);
                    GithubFollowing githubFollowing = GithubFollowingConvert.convert(item);
                    githubFollowingList.add(githubFollowing);
                    // 异步写库
                    CompletableFuture.runAsync(()-> {
                        followerService.writeGithubFollowing2Following(githubFollowing,developerId);
                    }).exceptionally(ex -> {
                        log.error("Github Follower Write Exception: {}", ex);
                        return null;
                    });
                }
                if(responseBody.size() < 100){
                    break;
                }
                page++;
            }
            githubFollowingResponse.setGithubFollowingList(githubFollowingList);
            githubFollowingRpcResult.setCode(RpcResultCode.SUCCESS);
            githubFollowingRpcResult.setData(githubFollowingResponse);
            return githubFollowingRpcResult;
        } catch (IOException e) {
            log.info("Github GetFollowing Exception: {}", e);
            githubFollowingRpcResult.setCode(RpcResultCode.FAILED);
            return githubFollowingRpcResult;
        }
    }
}
