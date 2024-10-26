package com.gitgle.service;

import com.gitgle.response.GithubReposResponse;

public interface GithubProjectService {

    GithubReposResponse getProjectByDeveloperId(String developerId);
}
