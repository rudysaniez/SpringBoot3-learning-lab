package com.adeo.springboot.learning.sb3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertiesConfig {

    @ConfigurationProperties(prefix = "security")
    public record UserSecurityAccess(String user,
                                     String userPassword,
                                     String admin,
                                     String adminPassword,
                                     String writer,
                                     String writerPassword) {}


    @ConfigurationProperties(prefix = "api.pagination")
    public record Pagination(int page, int size) {}
}
