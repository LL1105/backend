package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class GithubRepoRank implements Serializable {

    private Integer repoId;

    private String repoName;

    private String ownerLogin;

    private String ownerAvatarUrl;

    private Integer starsCount;

    private Integer forksCount;

    private Integer watchersCount;
}
