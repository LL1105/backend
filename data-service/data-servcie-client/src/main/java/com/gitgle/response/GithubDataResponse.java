package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class GithubDataResponse implements Serializable {

    private Integer githubUserTotal;

    private Integer githubRepoTotal;

    private Integer githubCommitTotal;

    private Integer githubOrganizationTotal;

    private Integer githubCountry;
}
