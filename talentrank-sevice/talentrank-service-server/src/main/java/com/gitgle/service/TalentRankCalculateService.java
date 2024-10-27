package com.gitgle.service;

public interface TalentRankCalculateService {

    /**
     * 计算项目重要性
     * @return 项目重要性得分
     */
    String calculateProjectImportance(String owner, String repoName);

    /**
     * 计算TalentRank得分
     * @return TalentRank
     */
    String calculateTalentRank(String owner);

    /**
     * 计算开发者贡献度
     * @return 开发者对应仓库的贡献度
     */
    String calculateContribution(String owner, String repoName);
}
