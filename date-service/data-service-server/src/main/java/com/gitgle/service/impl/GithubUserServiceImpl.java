package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.GithubUser;
import com.gitgle.result.RpcResult;
import com.gitgle.utils.GithubApiRequestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;

@Slf4j
@DubboService
public class GithubUserServiceImpl implements com.gitgle.service.GithubUserService {

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Override
    public RpcResult<GithubUser> searchByDeveloperId(String developerId) {
        RpcResult<GithubUser> githubUserRpcResult = new RpcResult<>();
        try {
            HashMap<String, String> searchParams = new HashMap<>();
            searchParams.put("q", "user:"+developerId);
            Response response = githubApiRequestUtils.searchUsers(searchParams);
            JSONObject responseBody = JSON.parseObject(response.body().string());
            log.info("Github SearchUsers Response: {}", responseBody);
            if(!response.isSuccessful()){
                githubUserRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
                return githubUserRpcResult;
            }
            GithubUser githubUser = JSON.parseObject(responseBody.getJSONArray("items").get(0).toString(), GithubUser.class);
            githubUserRpcResult.setData(githubUser);
            githubUserRpcResult.setCode(RpcResultCode.SUCCESS);
            return githubUserRpcResult;
        } catch (IOException e) {
            log.error("Github SearchUsers Exception: {}", e);
            githubUserRpcResult.setCode(RpcResultCode.FAILED);
            return githubUserRpcResult;
        }
    }
}
