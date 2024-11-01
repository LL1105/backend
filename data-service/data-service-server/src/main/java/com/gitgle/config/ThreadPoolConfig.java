package com.gitgle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolTaskExecutor refreshUserTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(5000);
        executor.setThreadNamePrefix("refresh-user-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}
