package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitgle.constant.RedisConstant;
import com.gitgle.convert.GithubRepoConvert;
import com.gitgle.dao.Repos;
import com.gitgle.response.GithubRepoRank;
import com.gitgle.response.GithubRepos;
import com.gitgle.response.PageRepoResponse;
import com.gitgle.service.ReposService;
import com.gitgle.mapper.ReposMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReposServiceImpl implements ReposService{

    @Resource
    private ReposMapper reposMapper;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void writeGithubRepos2Repos(GithubRepos githubRepos) {
        RLock lock = redissonClient.getLock(RedisConstant.GITHUB_REPO_LOCK + githubRepos.getRepoName() + ":" + githubRepos.getOwnerLogin());
        try {
            lock.lock(5, TimeUnit.SECONDS);
            Repos repo = reposMapper.selectOne(Wrappers.lambdaQuery(Repos.class).eq(Repos::getRepoName, githubRepos.getRepoName()).eq(Repos::getOwnerlogin, githubRepos.getOwnerLogin()));
            if (ObjectUtils.isNotEmpty(repo)) {
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
            repo = GithubRepoConvert.convert2Repos(githubRepos);
            reposMapper.insert(repo);
        }catch (Exception e){
            log.error("write github repos error: {}", e.getMessage());
        }finally {
            lock.unlock();
        }
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
    public List<GithubRepoRank> getReposOrderByStar() {
        Page<Repos> page = new Page<>(1, 100);
        Page<Repos> reposPage = reposMapper.selectPage(page, Wrappers.lambdaQuery(Repos.class).orderBy(true, false, Repos::getStarsCount));
        return reposPage.getRecords().stream().map(repo -> {
            return GithubRepoConvert.convert2Rank(repo);
        }).collect(Collectors.toList());
    }

    @Override
    public PageRepoResponse pageRepos2GithubRepos(Integer page, Integer size) {
        PageRepoResponse pageRepoResponse = new PageRepoResponse();
        Page<Repos> pageN = new Page<>(page, size);
        Page<Repos> reposPage = reposMapper.selectPage(pageN, Wrappers.lambdaQuery(Repos.class).orderBy(true, false, Repos::getStarsCount));
        pageRepoResponse.setTotalPage(reposPage.getPages());
        pageRepoResponse.setPageSize(size);
        pageRepoResponse.setPage(page);
        pageRepoResponse.setGithubReposList(reposPage.getRecords().stream().map(repo -> {
            return GithubRepoConvert.convert2Rank(repo);
        }).collect(Collectors.toList()));
        return pageRepoResponse;
    }

    @Override
    public List<GithubRepos> getReposByLogin(String owner) {
        List<Repos> reposList = reposMapper.selectList(Wrappers.lambdaQuery(Repos.class).eq(Repos::getOwnerlogin, owner));
        if(ObjectUtils.isNotEmpty(reposList)){
            return reposList.stream().map(GithubRepoConvert::convert).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}




