package com.gitgle.service;

import com.gitgle.response.GithubFollowersResponse;
import com.gitgle.response.GithubFollowingResponse;
import com.gitgle.response.GithubUser;
import com.gitgle.service.resp.GithubUserResp;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GithubUserInfo implements Serializable {

    private GithubUserResp githubUser;

    private List<GithubUser> githubFollowing;

    private List<GithubUser> githubFollowers;
}
