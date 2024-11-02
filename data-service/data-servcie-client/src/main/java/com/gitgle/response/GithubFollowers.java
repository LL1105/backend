package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class GithubFollowers implements Serializable {

    /**
     * 关注者login
     */
    private String login;
}
