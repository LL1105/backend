package com.gitgle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.dao.Follower;
import com.gitgle.response.GithubFollowers;
import com.gitgle.service.FollowerService;
import com.gitgle.mapper.FollowerMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
* @author maojunjun
* @description 针对表【follower】的数据库操作Service实现
* @createDate 2024-10-28 21:17:16
*/
@Service
public class FollowerServiceImpl implements FollowerService{

    @Resource
    private FollowerMapper followerMapper;

    @Override
    public List<GithubFollowers> readFollower2GithubFollowers(String login) {
        return Collections.emptyList();
    }

    @Override
    public void writeGithubFollower2Follower(GithubFollowers githubFollowers) {

    }


}




