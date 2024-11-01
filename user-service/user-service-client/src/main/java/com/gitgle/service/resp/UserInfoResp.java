package com.gitgle.service.resp;

import com.gitgle.response.GithubFollowersResponse;
import com.gitgle.response.GithubFollowingResponse;
import com.gitgle.response.GithubUser;
import com.gitgle.service.GithubUserInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserInfoResp implements Serializable {

    private String username;

    private String email;

    private String login;

    private String talentRank;

    private GithubUserInfo githubUserInfo;



}
