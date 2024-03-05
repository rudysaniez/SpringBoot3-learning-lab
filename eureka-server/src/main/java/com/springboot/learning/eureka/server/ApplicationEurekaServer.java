package com.springboot.learning.eureka.server;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class ApplicationEurekaServer {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApplicationEurekaServer.class);
        app.setBannerMode(Banner.Mode.CONSOLE);
        app.run(args);
    }
}
