package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.*;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubFollowingService;
import com.gitgle.service.GithubUserService;
import com.gitgle.service.NationCalculationService;
import com.gitgle.utils.SparkApiUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class NationCalculationServiceImpl implements NationCalculationService {

    @Resource
    private SparkApiUtils sparkApiUtils;

    @DubboReference
    private GithubUserService githubUserService;

    @DubboReference
    private GithubFollowingService githubFollowingService;

    @Override
    public NationResponse calculateNation(String login) {
        try {
            Map<String, Integer> relationshipLocationSet = new ConcurrentHashMap<>();
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
            String question = "根据上面[]中的github开发者的Follower和Following的地区信息，请你为我推测出这个开发所在的国家或者地区，并给出置信度，你只需要给我返回你推测出的国家或地区的中文名、英文名(英文名需要全称)以及置信度，并用-分隔（例如：中国-China-0.55),请特别注意，不要返回我需要的信息以外的信息，这将导致重大错误";
            stringBuilder.append(question);
            Response response = sparkApiUtils.doRequest(stringBuilder.toString());
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求spark api失败");
            }
            return response2NationResponse(response);
        } catch (Exception e) {
            return null;
        }
    }

    public NationResponse response2NationResponse(Response response) throws IOException {
        NationResponse nationResponse = new NationResponse();
        JSONObject responseBody = JSON.parseObject(response.body().string());
        String content = responseBody.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        log.info("Spark Response Content: {}", content);
        String[] nationArray = content.split("-");
        nationResponse.setNation(nationArray[0]);
        nationResponse.setNationEnglish(nationArray[1]);
        nationResponse.setConfidence(Double.valueOf(nationArray[2]));
        return nationResponse;
    }

    @Override
    public NationResponse getNationByLocation(String location) {
        try {
            String question = ":根据上面这个Location信息，请你为我推测出这个Location所在的国家或者地区，并给出置信度，你只需要给我返回你推测出的国家或地区的中文名、英文名(英文名需要全称)以及置信度，并用-分隔（例如：中国-China-0.55),请特别注意，不要返回我需要的信息以外的信息，这将导致重大错误";
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Location:");
            stringBuilder.append(location);
            stringBuilder.append("\n");
            stringBuilder.append(question);
            Response response = sparkApiUtils.doRequest(stringBuilder.toString());
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求spark api失败");
            }
            return response2NationResponse(response);
        }catch (Exception e){
            log.error("推测开发者国家或地区失败：{}", e.getMessage());
            return null;
        }
    }

    private void fetchLocationDataFromFollowing(String login, Map<String, Integer> locationSet) {
        RpcResult<GithubFollowingResponse> followingResponse = githubFollowingService.listUserFollowingByDeveloperId(login);
        if (!RpcResultCode.SUCCESS.equals(followingResponse.getCode())) {
            throw new RuntimeException("获取用户关注列表失败");
        }
        for (GithubFollowing following : followingResponse.getData().getGithubFollowingList()) {
            addLocationToSet(following.getLogin(), locationSet);
        }
    }

    private void fetchLocationDataFromFollowers(String login, Map<String, Integer> locationSet) {
        RpcResult<GithubFollowersResponse> followersResponse = githubFollowingService.getFollowersByDeveloperId(login);
        if (!RpcResultCode.SUCCESS.equals(followersResponse.getCode())) {
            throw new RuntimeException("获取开发者粉丝列表失败");
        }
        for (GithubFollowers follower : followersResponse.getData().getGithubFollowersList()) {
            addLocationToSet(follower.getLogin(), locationSet);
        }
    }

    private void addLocationToSet(String login, Map<String, Integer> locationSet) {
        RpcResult<GithubUser> userResult = githubUserService.getUserByLogin(login);
        if (RpcResultCode.SUCCESS.equals(userResult.getCode())) {
            GithubUser githubUser = userResult.getData();
            if (StringUtils.isNotEmpty(githubUser.getLocation())) {
                locationSet.compute(githubUser.getLocation(), (k, v) -> v == null ? 1 : v + 1);
            }
        }
    }
}
