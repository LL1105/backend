package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.dao.Commit;
import com.gitgle.dao.Organization;
import com.gitgle.dao.User;
import com.gitgle.mapper.UserMapper;
import com.gitgle.produce.KafkaProducer;
import com.gitgle.response.*;
import com.gitgle.result.RpcResult;
import com.gitgle.service.FollowerService;
import com.gitgle.service.OrganizationService;
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

@Slf4j
@DubboService
public class GithubUserServiceImpl implements com.gitgle.service.GithubUserService {

    @Resource
    private FollowerService followerService;

    @Resource
    private OrganizationService organizationService;

    @Resource
    private com.gitgle.service.UserService userService;

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Resource
    private KafkaProducer kafkaProducer;

    @Override
    public RpcResult<GithubUser> searchByDeveloperId(String developerId) {
        RpcResult<GithubUser> githubUserRpcResult = new RpcResult<>();
        try {
            // 先查库，没有再github上搜索
            GithubUser githubUser = userService.readGithubUser2GithubUser(developerId);
            if(ObjectUtils.isNotEmpty(githubUser)){
                githubUserRpcResult.setData(githubUser);
                githubUserRpcResult.setCode(RpcResultCode.SUCCESS);
                return githubUserRpcResult;
            }
            HashMap<String, String> searchParams = new HashMap<>();
            searchParams.put("q", "user:"+developerId);
            Response response = githubApiRequestUtils.searchUsers(searchParams);
            JSONObject responseBody = JSON.parseObject(response.body().string());
            log.info("Github SearchUsers Response: {}", responseBody);
            if(!response.isSuccessful()){
                githubUserRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
                return githubUserRpcResult;
            }
            githubUser = JSON.parseObject(responseBody.getJSONArray("items").get(0).toString(), GithubUser.class);
            // 异步写库
            final GithubUser finalGithubUser = githubUser;
            CompletableFuture.runAsync(()-> {
                userService.writeGithubUser2User(finalGithubUser);
            }).exceptionally(ex -> {
                log.error("Github SearchUsers Exception: {}", ex);
                return null;
            });
            githubUserRpcResult.setData(githubUser);
            githubUserRpcResult.setCode(RpcResultCode.SUCCESS);
            return githubUserRpcResult;
        } catch (IOException e) {
            log.error("Github SearchUsers Exception: {}", e);
            githubUserRpcResult.setCode(RpcResultCode.FAILED);
            return githubUserRpcResult;
        }
    }

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
                    GithubFollowers githubFollowers = new GithubFollowers();
                    githubFollowers.setId(item.getLong("id"));
                    githubFollowers.setLogin(item.getString("login"));
                    githubFollowers.setAvatarUrl(item.getString("avatar_url"));
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
    public RpcResult<GithubFollowersResponse> listUserFollowingByDeveloperId(String developerId) {
        RpcResult<GithubFollowersResponse> githubFollowersRpcResult = new RpcResult<>();
        GithubFollowersResponse githubFollowersResponse = new GithubFollowersResponse();
        try {
            // 先查库，没有再github上搜索
            List<GithubFollowers> githubFollowersList = followerService.readFollowing2GithubFollowing(developerId);
            if(ObjectUtils.isNotEmpty(githubFollowersList)){
                githubFollowersResponse.setGithubFollowersList(githubFollowersList);
                githubFollowersRpcResult.setCode(RpcResultCode.SUCCESS);
                githubFollowersRpcResult.setData(githubFollowersResponse);
                return githubFollowersRpcResult;
            }
            HashMap<String, String> queryParams = new HashMap<>();
            queryParams.put("per_page", "100");
            Integer page = 1;
            githubFollowersList = new ArrayList<>();
            while(true){
                queryParams.put("page", page.toString());
                Response response = githubApiRequestUtils.listUserFollowing(developerId, queryParams);
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
                    GithubFollowers githubFollowers = new GithubFollowers();
                    githubFollowers.setId(item.getLong("id"));
                    githubFollowers.setLogin(item.getString("login"));
                    githubFollowers.setAvatarUrl(item.getString("avatar_url"));
                    githubFollowersList.add(githubFollowers);
                    // 异步写库
                    CompletableFuture.runAsync(()-> {
                        followerService.writeGithubFollower2Follower(githubFollowers,developerId);
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
    public RpcResult<GithubOrganizationResponse> getOrganizationByDeveloperId(String developerId) {
        RpcResult<GithubOrganizationResponse> githubOrganizationResponseRpcResult = new RpcResult<>();
        GithubOrganizationResponse githubOrganizationResponse = new GithubOrganizationResponse();
        try {
            // 先查库，没有再github上搜索
            List<GithubOrganization> githubOrganizationList = organizationService.readOrganization2GithubOrganization(developerId);
            if(ObjectUtils.isNotEmpty(githubOrganizationList)){
                githubOrganizationResponse.setGithubOrganizationList(githubOrganizationList);
                githubOrganizationResponseRpcResult.setCode(RpcResultCode.SUCCESS);
                githubOrganizationResponseRpcResult.setData(githubOrganizationResponse);
                return githubOrganizationResponseRpcResult;
            }
            HashMap<String, String> queryParams = new HashMap<>();
            queryParams.put("per_page", "100");
            Integer page = 1;
            githubOrganizationList = new ArrayList<>();
            while(true){
                queryParams.put("page", page.toString());
                Response response = githubApiRequestUtils.getUserFollowers(developerId, queryParams);
                if(!response.isSuccessful()){
                    if(page.equals(1)){
                        githubOrganizationResponseRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
                        return githubOrganizationResponseRpcResult;
                    }else{
                        log.error("Github Api Failed In page:{}", page);
                        break;
                    }
                }
                JSONArray responseBody = JSON.parseArray(response.body().string());
                log.info("Github List Organization Response: {}", responseBody);
                for(int i=0; i<responseBody.size(); i++){
                    JSONObject item =responseBody.getJSONObject(i);
                    GithubOrganization githubOrganization = new GithubOrganization();
                    githubOrganization.setId(item.getLong("id"));
                    githubOrganization.setLogin(item.getString("login"));
                    githubOrganization.setAvatarUrl(item.getString("avatar_url"));
                    githubOrganizationList.add(githubOrganization);
                    // 异步写库
                    CompletableFuture.runAsync(()-> {
                        organizationService.writeGithubOrganization2Organization(githubOrganization);
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
            githubOrganizationResponse.setGithubOrganizationList(githubOrganizationList);
            githubOrganizationResponseRpcResult.setCode(RpcResultCode.SUCCESS);
            githubOrganizationResponseRpcResult.setData(githubOrganizationResponse);
            return githubOrganizationResponseRpcResult;
        } catch (IOException e) {
            log.info("Github GetFollowers Exception: {}", e);
            githubOrganizationResponseRpcResult.setCode(RpcResultCode.FAILED);
            return githubOrganizationResponseRpcResult;
        }
    }
}
