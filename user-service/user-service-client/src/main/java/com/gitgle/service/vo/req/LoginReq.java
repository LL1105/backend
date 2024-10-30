package com.gitgle.service.vo.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginReq implements Serializable {

    private String email;

    private String password;
}
