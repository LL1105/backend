package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.*;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubCommitService;
import com.gitgle.service.GithubFollowingService;
import com.gitgle.service.GithubUserService;
import com.gitgle.service.NationService;
import com.gitgle.utils.SparkApiUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@DubboService
@Slf4j
public class NationServiceImpl implements NationService {

    @Resource
    private SparkApiUtils sparkApiUtils;

    @DubboReference
    private GithubUserService githubUserService;

    @DubboReference
    private GithubFollowingService githubFollowingService;

    @Override
    public RpcResult<NationResponse> getNationByDeveloperId(String login) {
        RpcResult<NationResponse> nationResponseRpcResult = new RpcResult<>();
        NationResponse nationResponse = new NationResponse();
        try {
            Set<String> relationshipLocationSet = Collections.synchronizedSet(new HashSet<>());
            // 异步并行获取关注列表
            CompletableFuture<Void> followerFuture = CompletableFuture
                    .runAsync(() -> fetchLocationDataFromFollowing(login, relationshipLocationSet))
                    .exceptionally(ex -> {
                        log.error("获取开发者关注列表失败: {}", ex);
                        return null;
                    });
            // 异步并行获取粉丝列表
            CompletableFuture<Void> followingFuture = CompletableFuture.
                    runAsync(() -> fetchLocationDataFromFollowers(login, relationshipLocationSet))
                    .exceptionally(ex -> {
                        log.error("获取开发者粉丝列表失败: {}", ex);
                        return null;
                    });
            // 使用 allOf 等待所有任务完成
            CompletableFuture.allOf(followerFuture, followingFuture).join();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(relationshipLocationSet);
            stringBuilder.append("\n");
            String question = "根据上面[]中的github开发者的Follower和Following的地区信息，请你为我推测出这个开发所在的国家或者地区，并给出置信度，你只需要给我返回你推测出的国家或地区的中文名、英文名以及置信度，并用-分隔（例如：中国-China-0.55),请特别注意，不要返回我需要的信息以外的信息，这将导致重大错误";
            stringBuilder.append(question);
            Response response = sparkApiUtils.doRequest(stringBuilder.toString());
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求spark api失败");
            }
            JSONObject responseBody = JSON.parseObject(response.body().string());
            Integer code = responseBody.getInteger("code");
            if (code != 0) {
                nationResponseRpcResult.setCode(RpcResultCode.REQUEST_SPARK_FAILED);
                return nationResponseRpcResult;
            }
            String content = responseBody.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            log.info("Spark Response Content: {}", content);
            String[] nationArray = content.split("-");
            nationResponse.setNation(nationArray[0]);
            nationResponse.setNationEnglish(nationArray[1]);
            nationResponse.setConfidence(Double.valueOf(nationArray[2]));
            nationResponseRpcResult.setData(nationResponse);
            nationResponseRpcResult.setCode(RpcResultCode.SUCCESS);
            return nationResponseRpcResult;
        } catch (Exception e) {
            nationResponseRpcResult.setCode(RpcResultCode.FAILED);
            return nationResponseRpcResult;
        }
    }

    private void fetchLocationDataFromFollowing(String login, Set<String> locationSet) {
        RpcResult<GithubFollowingResponse> followingResponse = githubFollowingService.listUserFollowingByDeveloperId(login);
        if (!RpcResultCode.SUCCESS.equals(followingResponse.getCode())) {
            throw new RuntimeException("获取用户关注列表失败");
        }
        for (GithubFollowing following : followingResponse.getData().getGithubFollowingList()) {
            addLocationToSet(following.getLogin(), locationSet);
        }
    }

    private void fetchLocationDataFromFollowers(String login, Set<String> locationSet) {
        RpcResult<GithubFollowersResponse> followersResponse = githubFollowingService.getFollowersByDeveloperId(login);
        if (!RpcResultCode.SUCCESS.equals(followersResponse.getCode())) {
            throw new RuntimeException("获取开发者粉丝列表失败");
        }
        for (GithubFollowers follower : followersResponse.getData().getGithubFollowersList()) {
            addLocationToSet(follower.getLogin(), locationSet);
        }
    }

    private void addLocationToSet(String login, Set<String> locationSet) {
        RpcResult<GithubUser> userResult = githubUserService.getUserByLogin(login);
        if (RpcResultCode.SUCCESS.equals(userResult.getCode())) {
            GithubUser githubUser = userResult.getData();
            if (StringUtils.isNotEmpty(githubUser.getLocation())) {
                locationSet.add(githubUser.getLocation());
            }
        }
    }
}
