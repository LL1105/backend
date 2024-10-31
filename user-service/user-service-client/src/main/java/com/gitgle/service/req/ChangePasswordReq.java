package com.gitgle.service.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChangePasswordReq implements Serializable {

    private String oldPassword;

    private String newPassword;

}
