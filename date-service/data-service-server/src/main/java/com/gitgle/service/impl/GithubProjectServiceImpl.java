package com.gitgle.service.impl;

import com.gitgle.response.GithubProject;
import com.gitgle.response.GithubProjectResponse;
import com.gitgle.service.GithubProjectService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class GithubProjectServiceImpl implements GithubProjectService {

    @Override
    public GithubProjectResponse getProjectByDeveloperId(String developerId) {
        return null;
    }
}
