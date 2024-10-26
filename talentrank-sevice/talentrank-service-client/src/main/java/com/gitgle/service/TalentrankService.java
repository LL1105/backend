package com.gitgle.service;

public interface TalentrankService {

    /**
     * 根据用户id获取TalentRank
     * @param userId 用户id
     * @return TalentRank得分
     */
    String getTalentrankByUserId(Integer userId);
}
