package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GithubCommitResponse implements Serializable {

    private List<GithubCommit> githubCommitList;
}
