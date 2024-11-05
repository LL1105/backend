package com.gitgle.service.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GithubUserResp implements Serializable {

    /**
     * account_id
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * github用户名
     * login
     */
    @JsonProperty("login")
    private String login;

    /**
     * 头像url
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;

    /**
     * github主页url
     */
    @JsonProperty("html_url")
    private String htmlUrl;

    /**
     * 地区
     */
    @JsonProperty("location")
    private String location;

    /**
     * 个人简述
     */
    @JsonProperty("bio")
    private String bio;

    /**
     * 公司
     */
    @JsonProperty("company")
    private String company;

    /**
     * 邮箱
     */
    @JsonProperty("email")
    private String email;

    /**
     * 公共仓库数量
     */
    @JsonProperty("public_repos")
    private Integer publicRepos;

    /**
     * 粉丝数量
     */
    @JsonProperty("followers")
    private Integer followers;

    /**
     * 关注的用户数
     */
    @JsonProperty("following")
    private Integer following;

    /**
     * 账号创建时间
     */
    @JsonProperty("created_at")
    private String createdAt;

    private String nation;

    private String nationEnglish;

    private List<String> domains;

    private String talentRank;
}
