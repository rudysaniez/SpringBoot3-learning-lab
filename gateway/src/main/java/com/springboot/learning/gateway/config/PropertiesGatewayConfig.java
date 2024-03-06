package com.springboot.learning.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertiesGatewayConfig {

    @ConfigurationProperties(prefix = "microservices")
    public record Microservices(String dictionaryApiUri, String backendApiUri) {}
}
