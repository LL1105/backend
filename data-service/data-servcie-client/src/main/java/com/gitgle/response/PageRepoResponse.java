package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageRepoResponse implements Serializable {

    private Long totalPage;

    private Integer page;

    private Integer pageSize;

    private List<GithubRepoRank> githubReposList;
}
