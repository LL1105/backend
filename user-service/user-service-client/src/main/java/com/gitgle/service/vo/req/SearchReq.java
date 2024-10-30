package com.gitgle.service.vo.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchReq implements Serializable {

    private String domain;

    private Integer nationId;

    private String login;

}
