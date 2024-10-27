package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GithubReposResponse implements Serializable {

    private List<GithubRepos> githubProjectList;
}
