package com.gitgle.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GithubRepos implements Serializable {

    @JsonProperty("id")
    private String id;

    @JsonProperty("node_id")
    private String nodeId;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("private")
    private Boolean orPrivate;

    private String ownerLogin;
}
