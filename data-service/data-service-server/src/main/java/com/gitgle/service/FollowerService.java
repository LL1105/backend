package com.gitgle.service;

import com.gitgle.response.GithubFollowers;
import com.gitgle.response.GithubFollowing;

import java.util.List;


public interface FollowerService{

    List<GithubFollowers> readFollower2GithubFollowers(String login);

    void writeGithubFollower2Follower(GithubFollowers githubFollowers, String login);

    List<GithubFollowing> readFollowing2GithubFollowing(String login);

    void writeGithubFollowing2Following(GithubFollowing githubFollowing, String login);
}
