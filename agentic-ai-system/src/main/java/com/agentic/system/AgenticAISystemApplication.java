package com.agentic.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for Agentic AI System
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
public class AgenticAISystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgenticAISystemApplication.class, args);
    }
}
