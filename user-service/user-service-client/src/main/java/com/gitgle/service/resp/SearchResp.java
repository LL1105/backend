package com.gitgle.service.resp;

import com.gitgle.response.GithubRepoRank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchResp implements Serializable {

    private Long totalPage;

    private Long page;

    private Long pageSize;

    private List<SearchUser> searchUsers;

}
