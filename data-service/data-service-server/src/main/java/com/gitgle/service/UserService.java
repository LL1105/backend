package com.gitgle.service;

import com.gitgle.response.GithubUser;

import java.util.List;


public interface UserService{

    void writeGithubUser2User(List<GithubUser> githubUserList);

    GithubUser readGithubUser2GithubUser(String login);
}
