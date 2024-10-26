package com.gitgle.service.impl;

import com.gitgle.service.TalentRankService;
import com.gitgle.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class TalentRankServiceimpl implements TalentRankService {

    @DubboReference
    UserService userService;

    @Override
    public String getTalentrankByUserId(Integer userId) {
        return userService.getRank(userId);
    }
}
