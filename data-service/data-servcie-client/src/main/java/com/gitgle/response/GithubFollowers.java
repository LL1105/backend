package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class GithubFollowers implements Serializable {

    private String login;

    private Long id;

    private String avatarUrl;
}
