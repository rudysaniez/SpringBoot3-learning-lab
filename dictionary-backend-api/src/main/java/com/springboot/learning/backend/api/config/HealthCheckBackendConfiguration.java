package com.springboot.learning.backend.api.config;

import com.springboot.learning.backend.api.integration.DictionaryIntegration;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class HealthCheckBackendConfiguration {

    private final DictionaryIntegration dictionaryIntegration;

    public HealthCheckBackendConfiguration(DictionaryIntegration dictionaryIntegration) {
        this.dictionaryIntegration = dictionaryIntegration;
    }

    @Bean
    ReactiveHealthContributor coreServices() {

        Map<String, ReactiveHealthIndicator> registry = new HashMap<>();
        registry.put("dictionary-api", dictionaryIntegration::getDictionaryHealth);
        return CompositeReactiveHealthContributor.fromMap(registry);
    }
}
