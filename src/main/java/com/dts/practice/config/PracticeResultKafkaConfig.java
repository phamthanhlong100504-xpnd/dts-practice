package com.dts.practice.config;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class PracticeResultKafkaConfig {
    @Bean(name="taskScheduler")
    public org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler taskScheduler() {
        // Preserve the existing single scheduler for Practice's original jobs.
        var scheduler=new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);scheduler.setThreadNamePrefix("practice-default-");return scheduler;
    }
    @Bean(name="practiceHistoryScheduler")
    public org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler practiceHistoryScheduler() {
        var scheduler=new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);scheduler.setThreadNamePrefix("practice-history-");
        scheduler.setRemoveOnCancelPolicy(true);return scheduler;
    }
    @Bean
    public DefaultKafkaProducerFactory<String,String> practiceResultProducerFactory(
            KafkaProperties properties,ObjectProvider<SslBundles> bundles) {
        var config=new java.util.HashMap<String,Object>(properties.buildProducerProperties(bundles.getIfAvailable()));
        config.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG,"all");
        config.put(org.apache.kafka.clients.producer.ProducerConfig.MAX_BLOCK_MS_CONFIG,5000);
        return new DefaultKafkaProducerFactory<>(config,StringSerializer::new,StringSerializer::new);
    }
    @Bean(name="practiceResultKafkaTemplate")
    public KafkaTemplate<String,String> practiceResultKafkaTemplate(
            @Qualifier("practiceResultProducerFactory") DefaultKafkaProducerFactory<String,String> factory) {
        // Stored JSON is serialized once; no Java package headers cross service boundaries.
        return new KafkaTemplate<>(factory);
    }
}
