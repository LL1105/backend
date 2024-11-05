package com.gitgle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.gitgle.constant.RedisConstant;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.job.HotDomainJob;
import com.gitgle.request.GithubRequest;
import com.gitgle.response.*;
import com.gitgle.result.RpcResult;
import com.gitgle.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@DubboService
@Slf4j
public class RpcDomainServiceImpl implements RpcDomainService {

    private static final Integer HOT_DOMAIN_COUNT = 15;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private DomainCalculationService domainCalculationService;

    @Override
    public RpcResult<DomainResponse> getDomainByDeveloperId(String owner) {
        RpcResult<DomainResponse> domainResponseRpcResult = new RpcResult<>();
        try {
            DomainResponse domainResponse = domainCalculationService.calculationDomain(owner);
            domainResponseRpcResult.setCode(RpcResultCode.SUCCESS);
            domainResponseRpcResult.setData(domainResponse);
            return domainResponseRpcResult;
        } catch (Exception e) {
            log.error("推测开发者领域失败：{}", e);
            domainResponseRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
            return domainResponseRpcResult;
        }
    }

    @Override
    public RpcResult<HotDomainResponse> getHotDomain() {
        RpcResult<HotDomainResponse> hotDomainResponseRpcResult = new RpcResult<>();
        HotDomainResponse hotDomainResponse = new HotDomainResponse();
        List<HotDomain> hotDomainList = redisTemplate.opsForList().range(RedisConstant.HOT_DOMAIN, 0, HOT_DOMAIN_COUNT);
        if(ObjectUtils.isNotEmpty(hotDomainList)){
            hotDomainResponse.setHotDomainList(hotDomainList);
            hotDomainResponseRpcResult.setData(hotDomainResponse);
            hotDomainResponseRpcResult.setCode(RpcResultCode.SUCCESS);
            return hotDomainResponseRpcResult;
        }
        hotDomainList = new ArrayList<>();
        hotDomainResponse.setHotDomainList(hotDomainList);
        hotDomainResponseRpcResult.setData(hotDomainResponse);
        hotDomainResponseRpcResult.setCode(RpcResultCode.SUCCESS);
        return hotDomainResponseRpcResult;
    }

}
