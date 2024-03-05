package com.springboot.learning.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertiesServiceConfig {

    @ConfigurationProperties(prefix = "application.opensearch.indices")
    public record ApplicationOpensearchConfiguration(String attributesDictionaryName) {}
}
