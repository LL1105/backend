package com.gitgle.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DomainDto implements Serializable {

    private String domain;

    private Integer domainId;

    private Double confidence;

    private String login;
}
