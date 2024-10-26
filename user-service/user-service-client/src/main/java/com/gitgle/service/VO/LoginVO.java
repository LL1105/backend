package com.gitgle.service.VO;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginVO implements Serializable {

    private String email;

    private String password;
}
