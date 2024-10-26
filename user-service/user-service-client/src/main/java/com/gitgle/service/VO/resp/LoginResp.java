package com.gitgle.service.VO.resp;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginResp implements Serializable {

    private Integer userId;

    private String userName;

    private String token;
}
