package com.gitgle.service.impl;

import com.gitgle.response.GithubProject;
import com.gitgle.response.GithubProjectResponse;
import com.gitgle.service.GithubProjectService;
import com.gitgle.service.TalentrankCalculateService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TalentrankCalculateServiceImpl implements TalentrankCalculateService {

    @DubboReference
    private GithubProjectService githubProjectService;

    @Override
    public String calculateProjectImportance(String projectId) {
        return "9.99999";
    }

    @Override
    public String calculateTalentRank(String developerId) {
        BigDecimal talentRank = new BigDecimal(0);
        GithubProjectResponse githubProjectResponse = githubProjectService.getProjectByDeveloperId(developerId);
        List<GithubProject> githubProjectList = githubProjectResponse.getGithubProjectList();
        for(GithubProject githubProject: githubProjectList){
            String projectImportance = calculateProjectImportance(githubProject.getProjectId());
            String contribution = calculateContribution(githubProject.getProjectId(), developerId);
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
