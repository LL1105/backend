package com.gitgle.service;

import com.gitgle.response.GithubFollowersResponse;
import com.gitgle.response.GithubFollowingResponse;
import com.gitgle.response.GithubUser;
import lombok.Data;

import java.io.Serializable;

@Data
public class GithubUserInfo implements Serializable {

    private GithubUser githubUser;

    private GithubFollowingResponse githubFollowing;

    private GithubFollowersResponse githubFollowers;
}
