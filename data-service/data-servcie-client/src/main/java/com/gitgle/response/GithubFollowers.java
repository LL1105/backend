package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class GithubFollowers implements Serializable {

    private String login;

    private Integer id;

    private String avatarUrl;
}
