package com.gitgle.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class GithubRequest implements Serializable {

    private String owner;

    private String repoName;

    private String author;
}
