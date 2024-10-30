package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class GithubContributor implements Serializable {

    private Integer id;

    private String login;

    private String repoName;

    private String repoOwner;

    private Integer contributions;
}
