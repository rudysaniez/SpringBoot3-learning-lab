package com.springboot.learning.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PropertiesConfig {

    @ConfigurationProperties(prefix = "security.access")
    public record UserSecurityAccessConfiguration(List<User> users) {}

    public record User(String username, String password, List<String> authorities) {}

    @ConfigurationProperties(prefix = "api.pagination")
    public record Pagination(int page, int size) {}

    @ConfigurationProperties(prefix = "application.opensearch.indices")
    public record ApplicationOpensearchConfiguration(String attributesDictionaryName) {}
}
