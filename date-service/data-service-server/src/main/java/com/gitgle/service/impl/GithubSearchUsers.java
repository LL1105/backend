package com.gitgle.service.impl;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.gitgle.utils.GithubApiRequestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;

/**
 * 搜索用户
 */
@Slf4j
@DubboService
public class GithubSearchUsers implements com.gitgle.service.GithubSearchUsers {

    @Value("${user.age}")
    private String user;

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Override
    public String search() {
        log.info("user: " + user);
        try {
            Response response = githubApiRequestUtils.searchUsers(new HashMap<>());
            log.info("response: " + response);
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
