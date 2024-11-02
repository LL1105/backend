package com.gitgle.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GithubCommit implements Serializable {

    private String authorLogin;

    private String sha;

    private Integer reposId;

    private String reposName;

    private String reposOwner;

    /**
     * 示例："2024-09-01T17:02:03.000+08:00"
     */
    private String commitDataTime;
}
