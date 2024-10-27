package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.dao.Commit;
import com.gitgle.dao.User;
import com.gitgle.mapper.UserMapper;
import com.gitgle.response.GithubCommit;
import com.gitgle.response.GithubUser;
import com.gitgle.result.RpcResult;
import com.gitgle.utils.GithubApiRequestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@DubboService
public class GithubUserServiceImpl implements com.gitgle.service.GithubUserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Override
    public RpcResult<GithubUser> searchByDeveloperId(String developerId) {
        RpcResult<GithubUser> githubUserRpcResult = new RpcResult<>();
        try {
            // 先查库，没有再github上搜索
            GithubUser githubUser = readGithubUser(developerId);
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
                writeGithubUser(finalGithubUser);
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

    public GithubUser readGithubUser(String login){
        User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getLogin, login));
        if(ObjectUtils.isEmpty(user)){
            return null;
        }
        GithubUser githubUser = new GithubUser();
        githubUser.setLogin(user.getLogin());
        return githubUser;
    }

    public void writeGithubUser(GithubUser githubUser) {
        // 先根据login查询数据库中是否存在
        User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getLogin, githubUser.getLogin()));
        if (ObjectUtils.isNotEmpty(user)) {
            return;
        }
        // 如果没有则入库
        user = new User();
        user.setLogin(githubUser.getLogin());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }
}
