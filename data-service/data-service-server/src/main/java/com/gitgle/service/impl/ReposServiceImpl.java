package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitgle.convert.GithubRepoConvert;
import com.gitgle.dao.Repos;
import com.gitgle.response.GithubRepos;
import com.gitgle.response.GithubReposContent;
import com.gitgle.service.ReposService;
import com.gitgle.mapper.ReposMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
            repo.setRepoId(githubRepos.getId());
            repo.setUrl(githubRepos.getUrl());
            repo.setOwnerAvatarUrl(githubRepos.getOwnerAvatarUrl());
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
        repo.setRepoId(githubRepos.getId());
        repo.setUrl(githubRepos.getUrl());
        repo.setOwnerAvatarUrl(githubRepos.getOwnerAvatarUrl());
        reposMapper.insert(repo);
    }

    @Override
    public GithubRepos readRepos2GithubRepos(String owner, String repoName) {
        Repos repo = reposMapper.selectOne(Wrappers.lambdaQuery(Repos.class).eq(Repos::getRepoName, repoName).eq(Repos::getOwnerlogin, owner));
        if(ObjectUtils.isEmpty(repo)){
            return null;
        }
        return GithubRepoConvert.convert(repo);
    }

    @Override
    public List<GithubRepos> getReposOrderByStar() {
        Page<Repos> page = new Page<>(1, 100);
        Page<Repos> reposPage = reposMapper.selectPage(page, Wrappers.lambdaQuery(Repos.class).orderBy(true, false, Repos::getStarsCount));
        return reposPage.getRecords().stream().map(repo -> {
            return GithubRepoConvert.convert(repo);
        }).collect(Collectors.toList());
    }
}




