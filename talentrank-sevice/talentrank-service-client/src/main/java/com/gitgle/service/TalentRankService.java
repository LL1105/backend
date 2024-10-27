package com.gitgle.service;

public interface TalentRankService {

    /**
     * 根据用户id获取TalentRank
     * @param developerId 开发者login
     * @return TalentRank得分
     */
    String getTalentrankByDeveloperId(String developerId);
}
