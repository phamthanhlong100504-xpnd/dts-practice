package com.dts.practice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class PracticeExpSchedulingConfig {
    @Bean("practiceExpScheduler")
    public ThreadPoolTaskScheduler practiceExpScheduler() {
        var scheduler=new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);scheduler.setThreadNamePrefix("practice-exp-");
        scheduler.setRemoveOnCancelPolicy(true);return scheduler;
    }
}
