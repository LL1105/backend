package com.gitgle.job;

import com.gitgle.produce.KafkaProducer;
import com.gitgle.service.GithubUserService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RefreshUserJob {

    @Resource
    private KafkaProducer kafkaProducer;

    @Resource
    private GithubUserService githubUserService;

    @XxlJob("refresh-user-job")
    public void refresh(){
        log.info("执行刷新User任务...");
        Map<String, String> searchParams = new HashMap<>();
    }
}
