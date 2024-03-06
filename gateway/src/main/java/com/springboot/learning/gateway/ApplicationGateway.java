package com.springboot.learning.gateway;

import com.springboot.learning.gateway.config.PropertiesGatewayConfig;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(value = {PropertiesGatewayConfig.Microservices.class})
@SpringBootApplication
public class ApplicationGateway {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApplicationGateway.class);
        app.setBannerMode(Banner.Mode.CONSOLE);
        app.run(args);
    }
}
