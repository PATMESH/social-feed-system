package com.dev.graphservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean("fanoutExecutor")
    public Executor fanoutExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}
