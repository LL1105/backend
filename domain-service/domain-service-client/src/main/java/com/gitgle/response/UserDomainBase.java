package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDomainBase implements Serializable {

    private String domain;

    private String confidence;
}
