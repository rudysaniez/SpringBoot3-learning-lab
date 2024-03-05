package com.springboot.learning.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorDictionaryConfig {

    @Bean
    Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
