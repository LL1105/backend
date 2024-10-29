package com.gitgle.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestJob {

    @XxlJob("testJob")
    public void test(){
        log.info("======>test<======");
    }
}
