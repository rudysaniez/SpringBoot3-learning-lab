package com.springboot.learning.authorization;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApplicationDefaultAuthorizationServer {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApplicationDefaultAuthorizationServer.class);
        app.setBannerMode(Banner.Mode.CONSOLE);
        app.run(args);
    }
}
