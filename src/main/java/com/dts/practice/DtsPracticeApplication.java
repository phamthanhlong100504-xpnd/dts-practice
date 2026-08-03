package com.dts.practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ConfigurationPropertiesScan
@EnableScheduling
public class DtsPracticeApplication {
    public static void main(String[] args) {
        SpringApplication.run(DtsPracticeApplication.class, args);
    }
}
