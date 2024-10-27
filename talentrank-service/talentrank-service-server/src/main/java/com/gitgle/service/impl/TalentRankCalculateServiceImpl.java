package com.gitgle.service.impl;

import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.GithubCommit;
import com.gitgle.response.GithubCommitResponse;
import com.gitgle.response.GithubRepos;
import com.gitgle.response.GithubReposResponse;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubCommitService;
import com.gitgle.service.GithubProjectService;
import com.gitgle.service.TalentRankCalculateService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TalentRankCalculateServiceImpl implements TalentRankCalculateService {

    @DubboReference
    private GithubProjectService githubProjectService;

    @DubboReference
    private GithubCommitService githubCommitService;

    @Override
    public String calculateProjectImportance(String owner, String repoName) {
        return "9.99999";
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
        Set<String> githubRepoNameSet = new HashSet<>();
        for(GithubCommit githubCommit : githubCommitResponse.getGithubCommitList()){
            githubRepoNameSet.add(githubCommit.getReposName());
        }
        // 计算每个仓库的重要度以及开发者在每个仓库的贡献度
        for(String githubRepoName : githubRepoNameSet){
            BigDecimal projectImportance = new BigDecimal(calculateProjectImportance(owner, githubRepoName));
            BigDecimal contribution = new BigDecimal(calculateContribution(owner, githubRepoName));
            talentRank = talentRank.add(projectImportance.multiply(contribution));
        }
        return talentRank.toString();
    }

    @Override
    public String calculateContribution(String owner, String repoName) {
        return "9.99999";
    }
}