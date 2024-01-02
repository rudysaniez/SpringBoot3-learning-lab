package com.adeo.springboot.learning.sb3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PropertiesConfig {

    @ConfigurationProperties(prefix = "security")
    public record UserSecurityAccess(String user,
                                     String userPassword,
                                     String admin,
                                     String adminPassword,
                                     String writer,
                                     String writerPassword) {}

    @ConfigurationProperties(prefix = "security.access")
    public record UserSecurityAccessConfiguration(List<User> users, List<String> roles) {}

    public record User(String username, String password, List<String> authorities) {}

    @ConfigurationProperties(prefix = "api.pagination")
    public record Pagination(int page, int size) {}
}
