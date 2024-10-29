package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.dao.Repos;
import com.gitgle.response.GithubRepos;
import com.gitgle.service.ReposService;
import com.gitgle.mapper.ReposMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author maojunjun
* @description 针对表【repos】的数据库操作Service实现
* @createDate 2024-10-29 00:55:29
*/
@Service
public class ReposServiceImpl implements ReposService{

    @Resource
    private ReposMapper reposMapper;


    @Override
    public void writeGithubRepos2Repos(GithubRepos githubRepos) {
        Repos repos = reposMapper.selectOne(Wrappers.lambdaQuery(Repos.class).eq(Repos::getName, githubRepos.getName()).eq(Repos::getOwnerlogin, githubRepos.getOwnerLogin()));
        if(ObjectUtils.isNotEmpty(repos)){
            return;
        }
        Repos repo = new Repos();
        repo.setName(githubRepos.getName());
        repo.setOrPrivate(githubRepos.getOrPrivate());
        repo.setCreateAt(githubRepos.getCreatedAt());
        repo.setUpdateAt(githubRepos.getUpdateAt());
        repo.setStarsCount(githubRepos.getStarsCount());
        repo.setForksCount(githubRepos.getForksCount());
        reposMapper.insert(repo);
    }

    @Override
    public GithubRepos readRepos2GithubRepos(String owner, String repoName) {
        Repos repo = reposMapper.selectOne(Wrappers.lambdaQuery(Repos.class).eq(Repos::getName, repoName).eq(Repos::getOwnerlogin, owner));
        if(ObjectUtils.isEmpty(repo)){
            return null;
        }
        GithubRepos githubRepos = new GithubRepos();
        githubRepos.setName(repo.getName());
        githubRepos.setOrPrivate(repo.getOrPrivate());
        githubRepos.setCreatedAt(repo.getCreateAt().toString());
        githubRepos.setUpdateAt(repo.getUpdateAt().toString());
        githubRepos.setStarsCount(repo.getStarsCount());
        githubRepos.setForksCount(repo.getForksCount());
        githubRepos.setIssueCount(repo.getIssueCount());
        return githubRepos;
    }
}




