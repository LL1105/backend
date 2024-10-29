package com.gitgle.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GithubOrganization implements Serializable {

    private String login;

    private Long id;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String description;
}
