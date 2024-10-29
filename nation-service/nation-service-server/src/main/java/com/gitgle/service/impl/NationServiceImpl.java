package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.*;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubCommitService;
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

    @Override
    public RpcResult<NationResponse> getNationByDeveloperId(String login) {
        RpcResult<NationResponse> nationResponseRpcResult = new RpcResult<>();
        NationResponse nationResponse = new NationResponse();
        try {
            Set<String> relationshipLocationSet = new HashSet<>();
            // 异步并行获取
            CompletableFuture.runAsync(()->{
                // 获取开发者的粉丝列表
                RpcResult<GithubFollowersResponse> followersByDeveloperId = githubUserService.getFollowersByDeveloperId(login);
                if (!RpcResultCode.SUCCESS.equals(followersByDeveloperId.getCode())) {
                    throw new RuntimeException("获取开发者粉丝列表失败");
                }
                for(GithubFollowers githubFollowers : followersByDeveloperId.getData().getGithubFollowersList()){
                    // 获取粉丝的location
                    RpcResult<GithubUser> searchByDeveloperId = githubUserService.getUserByLogin(githubFollowers.getLogin());
                    if (!RpcResultCode.SUCCESS.equals(searchByDeveloperId.getCode())) {
                        continue;
                    }
                    GithubUser githubUser = searchByDeveloperId.getData();
                    if (StringUtils.isNotEmpty(githubUser.getLocation())) {
                        relationshipLocationSet.add(githubUser.getLocation());
                    }
                }
            }).exceptionally(ex->{
                log.error("获取开发者粉丝列表失败: {}", ex);
                return null;
            });
            CompletableFuture.runAsync(()->{
                // 获取开发者的关注列表
                RpcResult<GithubFollowersResponse> followingByDeveloperId = githubUserService.listUserFollowingByDeveloperId(login);
                if(!RpcResultCode.SUCCESS.equals(followingByDeveloperId.getCode())){
                    throw new RuntimeException("获取用户关注列表失败");
                }
                for(GithubFollowers githubFollowers : followingByDeveloperId.getData().getGithubFollowersList()){
                    // 获取粉丝的location
                    RpcResult<GithubUser> searchByDeveloperId = githubUserService.getUserByLogin(githubFollowers.getLogin());
                    if (!RpcResultCode.SUCCESS.equals(searchByDeveloperId.getCode())) {
                        continue;
                    }
                    GithubUser githubUser = searchByDeveloperId.getData();
                    if (StringUtils.isNotEmpty(githubUser.getLocation())) {
                        relationshipLocationSet.add(githubUser.getLocation());
                    }
                }
            }).exceptionally(ex->{
                log.error("获取开发者关注列表失败: {}", ex);
                return null;
            });
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(relationshipLocationSet);
            stringBuilder.append("\n");
            String question = "根据上面[]中的github开发者的Follower和Following的地区信息，请你为我推测出这个开发所在的国家或者地区，并给出置信度，你只需要给我返回你推测出的国家或地区的中文名、英文名以及置信度，并用-分隔（例如：中国-China-0.55),请特别注意，不要返回我需要的信息以外的信息，这将导致重大错误";
            stringBuilder.append(question);
            Response response = sparkApiUtils.doRequest(stringBuilder.toString());
            if (!response.isSuccessful()) {
                nationResponseRpcResult.setCode(RpcResultCode.REQUEST_SPARK_FAILED);
                return nationResponseRpcResult;
            }
            JSONObject responseBody = JSON.parseObject(response.body().string());
            Integer code = responseBody.getInteger("code");
            if (code != 0) {
                nationResponseRpcResult.setCode(RpcResultCode.REQUEST_SPARK_FAILED);
                return nationResponseRpcResult;
            }
            String content = responseBody.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            log.info("Spark Response Content: {}", content);
            if(content.equals("0")){
                nationResponseRpcResult.setCode(RpcResultCode.REQUEST_SPARK_FAILED);
                return nationResponseRpcResult;
            }
            String[] nationArray = content.split("-");
            nationResponse.setNation(nationArray[0]);
            nationResponse.setNationEnglish(nationArray[1]);
            nationResponse.setConfidence(nationArray[2]);
            nationResponseRpcResult.setData(nationResponse);
            nationResponseRpcResult.setCode(RpcResultCode.SUCCESS);
            return nationResponseRpcResult;
        }catch (Exception e){
            nationResponseRpcResult.setCode(RpcResultCode.FAILED);
            return nationResponseRpcResult;
        }
    }
}
