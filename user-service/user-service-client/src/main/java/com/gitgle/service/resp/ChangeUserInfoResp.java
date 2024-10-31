package com.gitgle.service.resp;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChangeUserInfoResp implements Serializable {

    private String login;

    private String username;

    private String email;

}
