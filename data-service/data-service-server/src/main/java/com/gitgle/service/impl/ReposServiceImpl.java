package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitgle.dao.Repos;
import com.gitgle.response.GithubRepos;
import com.gitgle.service.ReposService;
import com.gitgle.mapper.ReposMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class ReposServiceImpl implements ReposService{

    @Resource
    private ReposMapper reposMapper;


    @Override
    public void writeGithubRepos2Repos(GithubRepos githubRepos) {
        Repos repo = reposMapper.selectOne(Wrappers.lambdaQuery(Repos.class).eq(Repos::getRepoName, githubRepos.getRepoName()).eq(Repos::getOwnerlogin, githubRepos.getOwnerLogin()));
        if(ObjectUtils.isNotEmpty(repo)){
            // 更新
            repo.setUpdateTime(LocalDateTime.now());
            repo.setWatchersCount(githubRepos.getWatchersCount());
            repo.setRepoName(githubRepos.getRepoName());
            repo.setOrPrivate(githubRepos.getOrPrivate());
            repo.setCreateAt(githubRepos.getCreatedAt());
            repo.setUpdateAt(githubRepos.getUpdateAt());
            repo.setStarsCount(githubRepos.getStarsCount());
            repo.setForksCount(githubRepos.getForksCount());
            repo.setIssueCount(githubRepos.getIssueCount());
            repo.setDescription(githubRepos.getDescription());
            return;
        }
        repo = new Repos();
        repo.setCreateTime(LocalDateTime.now());
        repo.setUpdateTime(LocalDateTime.now());
        repo.setOwnerlogin(githubRepos.getOwnerLogin());
        repo.setWatchersCount(githubRepos.getWatchersCount());
        repo.setRepoName(githubRepos.getRepoName());
        repo.setOrPrivate(githubRepos.getOrPrivate());
        repo.setCreateAt(githubRepos.getCreatedAt());
        repo.setUpdateAt(githubRepos.getUpdateAt());
        repo.setStarsCount(githubRepos.getStarsCount());
        repo.setForksCount(githubRepos.getForksCount());
        repo.setIssueCount(githubRepos.getIssueCount());
        repo.setDescription(githubRepos.getDescription());
        reposMapper.insert(repo);
    }

    @Override
    public GithubRepos readRepos2GithubRepos(String owner, String repoName) {
        Repos repo = reposMapper.selectOne(Wrappers.lambdaQuery(Repos.class).eq(Repos::getRepoName, repoName).eq(Repos::getOwnerlogin, owner));
        if(ObjectUtils.isEmpty(repo)){
            return null;
        }
        GithubRepos githubRepos = new GithubRepos();
        githubRepos.setWatchersCount(repo.getWatchersCount());
        githubRepos.setOwnerLogin(repo.getOwnerlogin());
        githubRepos.setId(repo.getId());
        githubRepos.setRepoName(repo.getRepoName());
        githubRepos.setOrPrivate(repo.getOrPrivate());
        githubRepos.setCreatedAt(repo.getCreateAt());
        githubRepos.setUpdateAt(repo.getUpdateAt());
        githubRepos.setStarsCount(repo.getStarsCount());
        githubRepos.setForksCount(repo.getForksCount());
        githubRepos.setIssueCount(repo.getIssueCount());
        githubRepos.setDescription(repo.getDescription());
        return githubRepos;
    }
}




