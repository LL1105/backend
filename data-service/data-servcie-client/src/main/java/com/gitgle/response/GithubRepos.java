package com.gitgle.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GithubRepos implements Serializable {

    @JsonProperty("id")
    private String id;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("private")
    private Boolean orPrivate;

    private String ownerLogin;

    private Integer starsCount;

    private Integer forksCount;

    private Integer issueCount;

    private Integer watchersCount;

    private String description;

    private String createdAt;

    private String updateAt;

    private String projectId;
}
