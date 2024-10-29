package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class GithubReposContent implements Serializable {

    private String type;

    private String encoding;

    private Integer size;

    private String name;

    private String path;

    private String content;

    private String sha;

    private String repoName;

    private String repoOwner;
}
