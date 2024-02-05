package com.springboot.learning.sb3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
