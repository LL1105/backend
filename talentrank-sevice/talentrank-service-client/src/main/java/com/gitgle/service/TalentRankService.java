package com.gitgle.service;

public interface TalentRankService {

    /**
     * 根据用户id获取TalentRank
     * @param userId 用户id
     * @return TalentRank得分
     */
    String getTalentrankByUserId(Integer userId);
}
