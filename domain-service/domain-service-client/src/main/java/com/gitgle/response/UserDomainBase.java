package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDomainBase implements Serializable {

    private String login;

    private Integer domainId;

    private String domain;

    private String confidence;
}
