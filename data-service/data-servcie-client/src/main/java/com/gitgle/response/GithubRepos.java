package com.gitgle.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GithubRepos implements Serializable {

    /**
     * 仓库id
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * 仓库名
     */
    @JsonProperty("name")
    private String repoName;

    /**
     * 是否私有
     */
    @JsonProperty("private")
    private Boolean orPrivate;

    /**
     * 仓库拥有者login
     */
    private String ownerLogin;

    /**
     * star数
     */
    private Integer starsCount;

    /**
     * fork数
     */
    private Integer forksCount;

    /**
     * issue数
     */
    private Integer issueCount;

    /**
     * watcher数
     */
    private Integer watchersCount;

    /**
     * 仓库描述
     */
    private String description;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updateAt;
}
