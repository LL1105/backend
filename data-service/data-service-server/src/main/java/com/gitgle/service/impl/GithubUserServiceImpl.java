package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.*;
import com.gitgle.result.RpcResult;
import com.gitgle.utils.GithubApiRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Slf4j
@DubboService
public class GithubUserServiceImpl implements com.gitgle.service.GithubUserService {

    @Resource
    private com.gitgle.service.UserService userService;

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Override
    public RpcResult<GithubUser> getUserByLogin(String login) {
        RpcResult<GithubUser> githubUserRpcResult = new RpcResult<>();
        try {
            // 先查库，如果用户信息不全，再从github上搜索
            GithubUser githubUser = userService.readGithubUser2GithubUser(login);
            if(ObjectUtils.isNotEmpty(githubUser)){
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
            }).exceptionally(e -> {
                log.error("Github User Write Exception: {}", e);
                return null;
            });
            githubUserRpcResult.setData(githubUser);
            githubUserRpcResult.setCode(RpcResultCode.SUCCESS);
            return githubUserRpcResult;
        }catch (IOException e){
            log.error("Github GetUserByUsername Exception: {}", e.getMessage());
            githubUserRpcResult.setCode(RpcResultCode.FAILED);
            return githubUserRpcResult;
        }
    }
}
