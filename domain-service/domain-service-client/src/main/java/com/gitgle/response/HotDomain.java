package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class HotDomain implements Serializable {

    private String domain;

    private Long developerTotal;
}
