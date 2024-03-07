package com.springboot.learning.backend.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertiesBackendConfig {

    @ConfigurationProperties(prefix = "api.pagination")
    public record Pagination(int page, int size) {}

    @ConfigurationProperties(prefix = "microservices")
    public record Microservices(String dictionaryApiUri,
                                String dictionaryApiKey) {}
}
