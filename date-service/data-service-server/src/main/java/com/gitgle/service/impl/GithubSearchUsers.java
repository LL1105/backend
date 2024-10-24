package com.gitgle.service.impl;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.gitgle.constant.GithubRestApi;
import com.gitgle.utils.GithubApiRequestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 搜索用户
 */
@Slf4j
@DubboService
public class GithubSearchUsers implements com.gitgle.service.GithubSearchUsers {

    @NacosValue(value = "${user:1}", autoRefreshed = true)
    private String user;

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Override
    public String search() {
        try {
            Response response = githubApiRequestUtils.searchUsers(null);
            log.info("response: " + response);
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
