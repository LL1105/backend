package com.gitgle.service;

import com.gitgle.result.RpcResult;

public interface TalentRankService {

    /**
     * 根据用户id获取TalentRank
     * @param developerId 开发者login
     * @return TalentRank得分
     */
    RpcResult<String> getTalentrankByDeveloperId(String developerId);
}
