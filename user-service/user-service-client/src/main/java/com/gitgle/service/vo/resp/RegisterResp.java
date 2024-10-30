package com.gitgle.service.vo.resp;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterResp implements Serializable {

    private Integer id;

    private String username;

    private String email;

    private String githubId;

    private String talentRank;
}
