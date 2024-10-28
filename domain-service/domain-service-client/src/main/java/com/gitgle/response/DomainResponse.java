package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class DomainResponse implements Serializable {

    private String domain;

    private String description;
}
