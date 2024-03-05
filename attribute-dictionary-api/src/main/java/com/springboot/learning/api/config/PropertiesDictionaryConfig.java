package com.springboot.learning.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PropertiesDictionaryConfig {

    @ConfigurationProperties(prefix = "security.access")
    public record UserSecurityAccessConfiguration(List<User> users) {}

    public record User(String username, String password, List<String> authorities) {}

    @ConfigurationProperties(prefix = "api.pagination")
    public record Pagination(int page, int size) {}
}
