package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class GithubFollowing implements Serializable {

    /**
     * 被关注者login
     */
    private String login;
}
