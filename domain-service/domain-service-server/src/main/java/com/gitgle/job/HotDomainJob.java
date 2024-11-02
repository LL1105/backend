package com.gitgle.job;

import com.gitgle.constant.RedisConstant;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.dao.Domain;
import com.gitgle.response.HotDomain;
import com.gitgle.result.RpcResult;
import com.gitgle.service.DomainService;
import com.gitgle.service.UserService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Component
@Slf4j
public class HotDomainJob {

    private static final Integer HOT_DOMAIN_COUNT = 15;

    @Resource
    private RedisTemplate redisTemplate;

    @DubboReference
    private UserService userService;

    @Resource
    private DomainService domainService;

    @XxlJob("refresh-hot-domain")
    public void refreshHotDomain() {
        List<Domain> domainList = domainService.readAllDomain();
        PriorityQueue<HotDomain> priorityQueue = new PriorityQueue<>(15, (h1, h2) -> {
            return Long.compare(h2.getDeveloperTotal(), h1.getDeveloperTotal()); // 假设按热度降序排列
        });
        for(Domain domain: domainList){
            RpcResult<Long> userCountInDomain = userService.getUserCountInDomain(domain.getId());
            if(!RpcResultCode.SUCCESS.equals(userCountInDomain.getCode())){
                log.error("获取领域用户数量失败,领域：{}", domain.getDomain());
                continue;
            }
            HotDomain hotDomain = new HotDomain();
            hotDomain.setDomain(domain.getDomain());
            hotDomain.setDeveloperTotal(userCountInDomain.getData());
            priorityQueue.add(hotDomain);
            if(priorityQueue.size() > HOT_DOMAIN_COUNT){
                priorityQueue.poll();
            }
        }
        redisTemplate.opsForList().rightPushAll(RedisConstant.HOT_DOMAIN, priorityQueue);
    }
}
