package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GithubFollowersResponse implements Serializable {

    List<GithubFollowers> githubFollowersList;
}
