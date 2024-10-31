package com.gitgle.service.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class RankReq implements Serializable {

    private Integer nationId;

    private Integer domainId;

    private String username;

}
