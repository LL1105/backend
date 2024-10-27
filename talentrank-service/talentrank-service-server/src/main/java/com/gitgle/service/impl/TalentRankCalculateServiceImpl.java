package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.request.GithubRequest;
import com.gitgle.response.GithubCommit;
import com.gitgle.response.GithubCommitResponse;
import com.gitgle.response.GithubRepos;
import com.gitgle.response.GithubReposResponse;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubCommitService;
import com.gitgle.service.GithubProjectService;
import com.gitgle.service.TalentRankCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class TalentRankCalculateServiceImpl implements TalentRankCalculateService {

    private String projectImportanceStarter = "1";

    private String starWeight = "0.5";

    private String forkWeight = "0.5";

    @DubboReference
    private GithubProjectService githubProjectService;

    @DubboReference
    private GithubCommitService githubCommitService;

    @Override
    public String calculateProjectImportance(String owner, String repoName) {
        // 获取项目信息
        RpcResult<GithubRepos> githubReposResponseRpcResult = githubProjectService.getRepoByOwnerAndRepoName(owner, repoName);
        if(!RpcResultCode.SUCCESS.equals(githubReposResponseRpcResult.getCode())) {
            return "0";
        }
        GithubRepos githubRepos = githubReposResponseRpcResult.getData();
        // 根据star数和fork数计算项目重要度
        BigDecimal projectImportance = new BigDecimal(projectImportanceStarter);
        BigDecimal starsCount = new BigDecimal(githubRepos.getStarsCount());
        BigDecimal forksCount = new BigDecimal(githubRepos.getForksCount());
        projectImportance.add(starsCount.multiply(new BigDecimal(starWeight))).add(forksCount.multiply(new BigDecimal(forkWeight)));
        log.debug("projectImportance:{}", projectImportance);
        return projectImportance.toString();
    }

    @Override
    public String calculateTalentRank(String owner) {
        // 获取该开发者提交的所有commit
        RpcResult<GithubCommitResponse> githubCommitResponseRpcResult = githubCommitService.searchCommitsByDeveloperId(owner);
        if(!RpcResultCode.SUCCESS.equals(githubCommitResponseRpcResult.getCode())) {
            return "0";
        }
        BigDecimal talentRank = new BigDecimal(0);
        GithubCommitResponse githubCommitResponse = githubCommitResponseRpcResult.getData();
        // 统计该开发者参121.36.79.38:8848与提交的所有项目
        Map<String, String> githubRepoMap = new HashMap<>();
        for(GithubCommit githubCommit : githubCommitResponse.getGithubCommitList()){
            githubRepoMap.put(githubCommit.getReposName(), githubCommit.getReposOwner());
        }
        // 计算每个仓库的重要度以及开发者在每个仓库的贡献度
        for(Map.Entry<String, String> githubRepo : githubRepoMap.entrySet()){
            BigDecimal projectImportance = new BigDecimal(calculateProjectImportance(owner, githubRepo.getKey()));
            BigDecimal contribution = new BigDecimal(calculateContribution(githubRepo.getValue(), githubRepo.getKey(), owner));
            talentRank = talentRank.add(projectImportance.multiply(contribution));
        }
        return talentRank.toString();
    }

    @Override
    public String calculateContribution(String repoOwner, String repoName, String author) {
        if(StringUtils.isEmpty(repoOwner) || StringUtils.isEmpty(repoName) || StringUtils.isEmpty(author)){
            return "1";
        }
        GithubRequest githubRequest = new GithubRequest();
        githubRequest.setOwner(repoOwner);
        githubRequest.setRepoName(repoName);
        githubRequest.setAuthor(author);
        RpcResult<GithubCommitResponse> githubCommitResponseRpcResultAuthor = githubCommitService.listCommitsByRepoAndAuthor(githubRequest);
        if(!RpcResultCode.SUCCESS.equals(githubCommitResponseRpcResultAuthor.getCode())){
            return "0";
        }
        return String.valueOf(githubCommitResponseRpcResultAuthor.getData().getGithubCommitList().size());
    }
}
