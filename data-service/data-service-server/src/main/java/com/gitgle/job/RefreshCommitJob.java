package com.gitgle.job;

import com.gitgle.produce.KafkaProducer;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class RefreshCommitJob {

    @Resource
    private KafkaProducer kafkaProducer;

    @XxlJob("refresh-commit-job")
    public void refresh(){
        log.info("执行刷新Commit任务...");
    }
}
