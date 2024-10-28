package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@DubboService
@Slf4j
public class NationServiceImpl implements NationService {

    @Resource
    private SparkApiUtils sparkApiUtils;

    @DubboReference
    private GithubUserService githubUserService;

    @DubboReference
    private GithubCommitService githubCommitService;

    @Override
    public RpcResult<NationResponse> getNationByDeveloperId(String login) {
        RpcResult<NationResponse> nationResponseRpcResult = new RpcResult<>();
        NationResponse nationResponse = new NationResponse();
        try {
//            RpcResult<GithubFollowersResponse> followersByDeveloperId = githubUserService.getFollowersByDeveloperId(login);
//            if (!RpcResultCode.SUCCESS.equals(followersByDeveloperId.getCode())) {
//                nationResponseRpcResult.setCode(followersByDeveloperId.getCode());
//                return nationResponseRpcResult;
//            }
//            RpcResult<GithubOrganizationResponse> organizationByDeveloperId = githubUserService.getOrganizationByDeveloperId(login);
//            if (!RpcResultCode.SUCCESS.equals(organizationByDeveloperId.getCode())) {
//                nationResponseRpcResult.setCode(organizationByDeveloperId.getCode());
//                return nationResponseRpcResult;
//            }
            RpcResult<GithubCommitResponse> githubCommitResponseRpcResult = githubCommitService.searchCommitsByDeveloperId(login);
            if (!RpcResultCode.SUCCESS.equals(githubCommitResponseRpcResult.getCode())) {
                nationResponseRpcResult.setCode(githubCommitResponseRpcResult.getCode());
                return nationResponseRpcResult;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for(GithubCommit githubCommit : githubCommitResponseRpcResult.getData().getGithubCommitList()){
                stringBuilder.append(githubCommit.getCommitDataTime());
                stringBuilder.append(" , ");
            }
            String question = "根据以上github开发者Commit的活跃时间，请你为我推测出这个开发者是哪个国家或者地区的，并给出置信度，你只需要给我返回你推测出的国家或地区中文名、英文名以及置信度，并用-分隔（例如：中国-China-0.55),请特别注意，不要返回我需要的信息以外的信息，这将导致重大错误，如果你推测不出来，给我 返回0即可";
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
