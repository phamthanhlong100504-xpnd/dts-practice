package com.dts.practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DtsPracticeApplication {
    public static void main(String[] args) {
        SpringApplication.run(DtsPracticeApplication.class, args);
    }
}
