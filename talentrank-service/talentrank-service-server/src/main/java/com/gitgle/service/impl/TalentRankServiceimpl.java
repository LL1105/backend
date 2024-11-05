package com.gitgle.service.impl;

import com.gitgle.constant.RpcResultCode;
import com.gitgle.result.RpcResult;
import com.gitgle.service.TalentRankCalculateService;
import com.gitgle.service.TalentRankService;
import com.gitgle.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
@Slf4j
public class TalentRankServiceimpl implements TalentRankService {

    @Resource
    private TalentRankCalculateService talentRankCalculateService;

    @Override
    public RpcResult<String> getTalentrankByDeveloperId(String developerId) {
        RpcResult<String> rpcResult = new RpcResult<>();
        try {
            String talentRank = talentRankCalculateService.calculateTalentRank(developerId);
            rpcResult.setCode(RpcResultCode.SUCCESS);
            rpcResult.setData(talentRank);
            return rpcResult;
        }catch (Exception e){
            log.error("获取开发者talentrank失败:{}",e.getMessage());
            rpcResult.setCode(RpcResultCode.FAILED);
            return rpcResult;
        }
    }
}
