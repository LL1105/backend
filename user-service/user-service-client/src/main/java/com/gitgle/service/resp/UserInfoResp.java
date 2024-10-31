package com.gitgle.service.resp;

import com.gitgle.response.GithubUser;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfoResp implements Serializable {

    private String username;

    private String email;

    private String login;

    private String talentRank;

    private GithubUser githubUser;
}
