package com.gitgle.service;

public interface TalentrankCalculateService {

    /**
     * 计算项目重要性
     * @param projectId 项目id
     * @return 项目重要性得分
     */
    String calculateProjectImportance(String projectId);

    /**
     * 计算TalentRank得分
     * @return TalentRank
     */
    String calculateTalentRank(String developerId);

    /**
     * 计算开发者贡献度
     * @param projectId 项目id
     * @param developerId 开发者id
     * @return 开发者对应仓库的贡献度
     */
    String calculateContribution(String projectId, String developerId);
}
