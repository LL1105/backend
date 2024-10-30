package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.dao.Commit;
import com.gitgle.dao.Organization;
import com.gitgle.dao.User;
import com.gitgle.job.RefreshUserJob;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@DubboService
public class GithubUserServiceImpl implements com.gitgle.service.GithubUserService {

    @Resource
    private OrganizationService organizationService;

    @Resource
    private com.gitgle.service.UserService userService;

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Override
    public RpcResult<GithubUserResponse> searchUsers(Map<String, String> searchParams) {
        RpcResult<GithubUserResponse> githubUserRpcResult = new RpcResult<>();
        try {
            GithubUserResponse githubUserResponse = githubApiRequestUtils.searchUsers(searchParams);
            // 异步写库
            CompletableFuture.runAsync(()-> {
                userService.writeGithubUser2User(githubUserResponse.getGithubUserList());
            }).exceptionally(ex -> {
                log.error("Github SearchUsers Exception: {}", ex);
                return null;
            });
            githubUserRpcResult.setData(githubUserResponse);
            githubUserRpcResult.setCode(RpcResultCode.SUCCESS);
            return githubUserRpcResult;
        } catch (IOException e) {
            log.error("Github SearchUsers Exception: {}", e);
            githubUserRpcResult.setCode(RpcResultCode.FAILED);
            return githubUserRpcResult;
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

    @Override
    public RpcResult<GithubUser> getUserByLogin(String login) {
        RpcResult<GithubUser> githubUserRpcResult = new RpcResult<>();
        try {
            // 先查库，如果用户信息不全，再从github上搜索
            GithubUser githubUser = userService.readGithubUser2GithubUser(login);
            if(ObjectUtils.isNotEmpty(githubUser) && StringUtils.isNotEmpty(githubUser.getLocation())){
                githubUserRpcResult.setData(githubUser);
                githubUserRpcResult.setCode(RpcResultCode.SUCCESS);
                return githubUserRpcResult;
            }
            githubUser = githubApiRequestUtils.getUserByUsername(login);
            ArrayList githubUserList = new ArrayList<>();
            githubUserList.add(githubUser);
            // 异步更新库
            CompletableFuture.runAsync(()->{
                userService.writeGithubUser2User(githubUserList);
            }).exceptionally(ex -> {
                log.error("Github User Write Exception: {}", ex);
                return null;
            });
            githubUserRpcResult.setData(githubUser);
            githubUserRpcResult.setCode(RpcResultCode.SUCCESS);
            return githubUserRpcResult;
        }catch (IOException e){
            log.error("Github GetUserByAccountId Exception: {}", e);
            githubUserRpcResult.setCode(RpcResultCode.FAILED);
            return githubUserRpcResult;
        }
    }

    @Resource
    private RefreshUserJob refreshUserJob;

    @Override
    public RpcResult<GithubUser> listUserByFollowers() {
        refreshUserJob.refreshAccountId();
        refreshUserJob.refresh();
        return null;
    }
}
