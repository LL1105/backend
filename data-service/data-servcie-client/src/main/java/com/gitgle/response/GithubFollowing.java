package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class GithubFollowing implements Serializable {

    /**
     * 被关注者login
     */
    private String login;

    /**
     * 被关注者id
     */
    private Integer id;

    /**
     * 被关注者头像url
     */
    private String avatarUrl;
}
