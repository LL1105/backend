package com.gitgle.service.impl;

import com.gitgle.response.GithubRepos;
import com.gitgle.response.GithubReposResponse;
import com.gitgle.service.GithubProjectService;
import com.gitgle.service.TalentRankCalculateService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TalentRankCalculateServiceImpl implements TalentRankCalculateService {

    @DubboReference
    private GithubProjectService githubProjectService;

    @Override
    public String calculateProjectImportance(String projectId) {
        return "9.99999";
    }

    @Override
    public String calculateTalentRank(String developerId) {
        BigDecimal talentRank = new BigDecimal(0);
        GithubReposResponse githubReposResponse = githubProjectService.getProjectByDeveloperId(developerId);
        List<GithubRepos> githubProjectList = githubReposResponse.getGithubProjectList();
        for(GithubRepos githubRepos : githubProjectList){
            String projectImportance = calculateProjectImportance(githubRepos.getProjectId());
            String contribution = calculateContribution(githubRepos.getProjectId(), developerId);
            BigDecimal tmp = new BigDecimal(contribution);
            BigDecimal tmp2 = new BigDecimal(projectImportance);
            talentRank.add(tmp.multiply(tmp2));
        }
        return talentRank.toString();
    }

    @Override
    public String calculateContribution(String projectId, String developerId) {
        return "9.99999";
    }
}
