package com.gitgle.service.impl;

import com.gitgle.utils.GithubApiRequestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;

/**
 * 搜索用户
 */
@Slf4j
@DubboService
public class GithubUserService implements com.gitgle.service.GithubUserService {

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Override
    public String search() {
        try {
            Response response = githubApiRequestUtils.searchUsers(new HashMap<>());
            log.info("response: " + response);
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
