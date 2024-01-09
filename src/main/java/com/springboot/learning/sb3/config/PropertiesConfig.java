package com.springboot.learning.sb3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class PropertiesConfig {

    @ConfigurationProperties(prefix = "security.access")
    public record UserSecurityAccessConfiguration(List<User> users) {}

    public record User(String username, String password, List<String> authorities) {}

    @ConfigurationProperties(prefix = "api.pagination")
    public record Pagination(int page, int size) {}
}
