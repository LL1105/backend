package com.gitgle.service.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterReq implements Serializable {

    private String username;

    private String password;

    private String email;

    private String githubId;

    private String code;

}
