package com.gitgle.service.resp;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterResp implements Serializable {

    private Integer id;

    private String username;

    private String email;

    private String login;

    private String talentRank;

    private String avatar;
}
