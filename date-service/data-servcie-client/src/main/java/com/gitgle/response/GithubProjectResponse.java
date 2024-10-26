package com.gitgle.response;

import lombok.Data;

import java.util.List;

@Data
public class GithubProjectResponse {

    private List<GithubProject> githubProjectList;
}
