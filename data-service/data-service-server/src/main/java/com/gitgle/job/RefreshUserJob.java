package com.gitgle.job;

import com.gitgle.produce.KafkaProducer;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class RefreshUserJob {

    @Resource
    private KafkaProducer kafkaProducer;

    @XxlJob("refresh-user-job")
    public void refresh(){
        log.info("执行刷新User任务...");
    }
}
