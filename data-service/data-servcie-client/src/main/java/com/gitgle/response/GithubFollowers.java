package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class GithubFollowers implements Serializable {

    /**
     * 关注者login
     */
    private String login;

    /**
     * 关注者id
     */
    private Integer id;

    /**
     * 关注者头像url
     */
    private String avatarUrl;
}