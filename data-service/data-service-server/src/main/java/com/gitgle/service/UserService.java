package com.gitgle.service;

import com.gitgle.dao.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gitgle.response.GithubUser;

import java.util.List;

/**
* @author maojunjun
* @description 针对表【user】的数据库操作Service
* @createDate 2024-10-28 20:05:34
*/
public interface UserService{

    void writeGithubUser2User(List<GithubUser> githubUserList);

    GithubUser readGithubUser2GithubUser(String login);

}
