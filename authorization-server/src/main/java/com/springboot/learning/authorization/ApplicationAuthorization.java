package com.springboot.learning.authorization;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApplicationAuthorization {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApplicationAuthorization.class);
        app.setBannerMode(Banner.Mode.CONSOLE);
        app.run(args);
    }
}
