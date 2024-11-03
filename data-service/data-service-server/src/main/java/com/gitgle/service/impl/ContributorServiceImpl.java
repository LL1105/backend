package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitgle.convert.GithubContributorConvert;
import com.gitgle.dao.Contributor;
import com.gitgle.response.GithubContributor;
import com.gitgle.service.ContributorService;
import com.gitgle.mapper.ContributorMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author maojunjun
* @description 针对表【contributor】的数据库操作Service实现
* @createDate 2024-10-30 17:06:38
*/
@Service
public class ContributorServiceImpl implements ContributorService{

    @Resource
    private ContributorMapper contributorMapper;

    @Override
    public void writeGithubContributor2Contributor(List<GithubContributor> githubContributor) {
        for(GithubContributor item: githubContributor){
            Contributor contributor = contributorMapper.selectOne(Wrappers.lambdaQuery(Contributor.class).eq(Contributor::getContributorId, item.getId()));
            if(item.equals(contributor)){
                contributorMapper.update(contributor, Wrappers.lambdaUpdate(Contributor.class).eq(Contributor::getContributorId, item.getId()));
                continue;
            }
            contributorMapper.insert(GithubContributorConvert.convert2Contributor(item));
        }
    }

    @Override
    public List<GithubContributor> readContributor2GithubContributor(String repoName, String repoOwner) {
        List<Contributor> contributors = contributorMapper.selectList(Wrappers.lambdaQuery(Contributor.class).eq(Contributor::getRepoName, repoName)
                .eq(Contributor::getRepoOwner, repoOwner));
        return contributors.stream().map(GithubContributorConvert::convert).collect(Collectors.toList());
    }
}




