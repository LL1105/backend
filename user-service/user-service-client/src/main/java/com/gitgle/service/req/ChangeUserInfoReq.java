package com.gitgle.service.req;

import jdk.jfr.DataAmount;
import lombok.Data;

import java.io.Serializable;

@Data
public class ChangeUserInfoReq implements Serializable {

    private String login;

    private String username;

    private String email;

    private String oldPassword;

    private String newPassword;

}
