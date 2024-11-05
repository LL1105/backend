package com.gitgle.service.resp;

import com.gitgle.response.GithubRepos;
import com.gitgle.response.GithubUser;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ShowUserInfoResp implements Serializable {

    GithubUserResp githubUser;

    List<GithubRepos> githubReposList;
}
