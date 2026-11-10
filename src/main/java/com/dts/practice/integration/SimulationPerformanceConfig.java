package com.dts.practice.integration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SimulationPerformanceConfig {
    @Bean(name="simulationPerformanceScheduler")
    public ThreadPoolTaskScheduler scheduler() {
        var scheduler=new ThreadPoolTaskScheduler();scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("simulation-performance-");scheduler.setRemoveOnCancelPolicy(true);return scheduler;
    }
    @Bean public NewTopic simulationPerformanceTopic(@Value("${simulation.performance.topic:simulation-performance-v1}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }
}
