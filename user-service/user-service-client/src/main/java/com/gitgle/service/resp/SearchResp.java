package com.gitgle.service.resp;

import com.gitgle.response.GithubRepoRank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchResp implements Serializable {

    private Long totalPage;

    private Integer page;

    private Integer pageSize;

    private List<SearchUser> searchUsers;

}
