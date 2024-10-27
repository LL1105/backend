package com.gitgle.service.impl;

import com.gitgle.service.TalentRankCalculateService;
import com.gitgle.service.TalentRankService;
import com.gitgle.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class TalentRankServiceimpl implements TalentRankService {

    @Resource
    private TalentRankCalculateService talentRankCalculateService;

    @Override
    public String getTalentrankByDeveloperId(String developerId) {
        return talentRankCalculateService.calculateTalentRank(developerId);
    }
}
