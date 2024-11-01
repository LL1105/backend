package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.request.GithubRequest;
import com.gitgle.response.*;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubCommitService;
import com.gitgle.service.GithubRepoService;
import com.gitgle.service.TalentRankCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class TalentRankCalculateServiceImpl implements TalentRankCalculateService {

    private String projectImportanceStarter = "1";

    private String starWeight = "0.5";

    private String forkWeight = "0.5";

    @DubboReference
    private GithubRepoService githubProjectService;

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
        // 统计该开发者参与提交的所有项目
        Map<String, String> githubRepoMap = new HashMap<>();
        for(GithubCommit githubCommit : githubCommitResponse.getGithubCommitList()){
            githubRepoMap.put(githubCommit.getReposName(), githubCommit.getReposOwner());
        }
        // 计算每个仓库的重要度以及开发者在每个仓库的贡献度
        for(Map.Entry<String, String> githubRepo : githubRepoMap.entrySet()){
            try {
                CompletableFuture<BigDecimal> projectImportance = CompletableFuture.
                        supplyAsync(() -> new BigDecimal(calculateProjectImportance(owner, githubRepo.getKey())));
                CompletableFuture<BigDecimal> contribution = CompletableFuture.
                        supplyAsync(() -> new BigDecimal(calculateContribution(githubRepo.getValue(), githubRepo.getKey(), owner)));
                CompletableFuture.allOf(projectImportance, contribution).join();
                talentRank = talentRank.add(projectImportance.get().multiply(contribution.get()));
            }catch (Exception e){
                log.error("TalentRank计算出错:{}", e);
            }
        }
        return talentRank.toString();
    }

    @Override
    public String calculateContribution(String repoOwner, String repoName, String author) {
        if(StringUtils.isEmpty(repoOwner) || StringUtils.isEmpty(repoName) || StringUtils.isEmpty(author)){
            return "1";
        }
        // 获取该仓库贡献度
        RpcResult<GithubContributorResponse> githubContributorResponseRpcResult = githubProjectService.listRepoContributors(repoOwner, repoName);
        if(!RpcResultCode.SUCCESS.equals(githubContributorResponseRpcResult.getCode())){
            return "0";
        }
        // 计算总贡献度和提交者贡献度
        BigDecimal totalContribution = new BigDecimal(0);
        BigDecimal authorContribution = null;
        for(GithubContributor githubContributor : githubContributorResponseRpcResult.getData().getGithubContributorList()){
            totalContribution = totalContribution.add(new BigDecimal(githubContributor.getContributions()));
            if(githubContributor.getLogin().equals(author)){
                authorContribution = new BigDecimal(githubContributor.getContributions());
            }
        }
        return String.valueOf(authorContribution.divide(totalContribution, 5, BigDecimal.ROUND_HALF_UP));
    }
}
