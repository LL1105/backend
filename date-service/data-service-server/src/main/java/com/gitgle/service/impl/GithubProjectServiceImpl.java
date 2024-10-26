package com.gitgle.service.impl;

import com.gitgle.response.GithubReposResponse;
import com.gitgle.service.GithubProjectService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class GithubProjectServiceImpl implements GithubProjectService {

    @Override
    public GithubReposResponse getProjectByDeveloperId(String developerId) {
        return null;
    }
}
