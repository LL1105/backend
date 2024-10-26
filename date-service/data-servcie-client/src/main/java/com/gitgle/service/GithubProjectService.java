package com.gitgle.service;

import com.gitgle.response.GithubProjectResponse;

public interface GithubProjectService {

    GithubProjectResponse getProjectByDeveloperId(String developerId);
}
