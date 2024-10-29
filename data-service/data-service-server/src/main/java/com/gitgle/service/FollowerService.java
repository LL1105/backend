package com.gitgle.service;

import com.gitgle.dao.Follower;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gitgle.response.GithubFollowers;
import com.gitgle.response.GithubFollowing;

import java.util.List;

/**
* @author maojunjun
* @description 针对表【follower】的数据库操作Service
* @createDate 2024-10-28 21:17:16
*/
public interface FollowerService{

    List<GithubFollowers> readFollower2GithubFollowers(String login);

    void writeGithubFollower2Follower(GithubFollowers githubFollowers, String login);

    List<GithubFollowing> readFollowing2GithubFollowing(String login);

    void writeGithubFollowing2Following(GithubFollowing githubFollowing, String login);
}
