package com.gitgle.job;

import com.gitgle.produce.KafkaProducer;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class RefreshRepoContentJob {

    @Resource
    private KafkaProducer kafkaProducer;

    @XxlJob("refresh-repoContent-job")
    public void refresh(){
        log.info("执行刷新RepoContent任务...");
    }
}
