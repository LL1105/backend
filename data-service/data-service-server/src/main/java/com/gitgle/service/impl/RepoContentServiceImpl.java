package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.dao.RepoContent;
import com.gitgle.response.GithubReposContent;
import com.gitgle.service.RepoContentService;
import com.gitgle.mapper.RepoContentMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
* @author maojunjun
* @description 针对表【repo_content】的数据库操作Service实现
* @createDate 2024-10-29 01:00:43
*/
@Service
public class RepoContentServiceImpl implements RepoContentService{

    @Resource
    private RepoContentMapper repoContentMapper;

    @Override
    public void writeGithubReposContent2RepoContent(GithubReposContent githubReposContent) {
        RepoContent repoContent = repoContentMapper.selectOne(Wrappers.lambdaQuery(RepoContent.class).eq(RepoContent::getSha, githubReposContent.getSha()));
        if(ObjectUtils.isNotEmpty(repoContent)){
            return;
        }
        repoContent = new RepoContent();
        repoContent.setRepoName(githubReposContent.getRepoName());
        repoContent.setRepoOwner(githubReposContent.getRepoOwner());
        repoContent.setPath(githubReposContent.getPath());
        repoContent.setSha(githubReposContent.getSha());
        repoContent.setContent(githubReposContent.getContent());
        repoContent.setCreateTime(LocalDateTime.now());
        repoContent.setUpdateTime(LocalDateTime.now());
        repoContentMapper.insert(repoContent);
    }

    @Override
    public GithubReposContent readRepoContent2GithubReposContent(String path, String repoName, String ownerLogin) {
        RepoContent repoContent = repoContentMapper.selectOne(Wrappers.lambdaQuery(RepoContent.class).eq(RepoContent::getPath, path).eq(RepoContent::getRepoName, repoName).eq(RepoContent::getRepoOwner, ownerLogin));
        if(ObjectUtils.isEmpty(repoContent)){
            return null;
        }
        GithubReposContent githubReposContent = new GithubReposContent();
        githubReposContent.setPath(repoContent.getPath());
        githubReposContent.setSha(repoContent.getSha());
        githubReposContent.setContent(repoContent.getContent());
        return githubReposContent;
    }
}




