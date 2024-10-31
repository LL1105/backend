package com.gitgle.service.resp;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfoResp implements Serializable {

    private String username;

    private String email;

    private String githubId;

    private String talentRank;
}
