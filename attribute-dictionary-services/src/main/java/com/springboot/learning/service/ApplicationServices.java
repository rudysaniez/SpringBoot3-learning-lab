package com.springboot.learning.service;

import com.springboot.learning.repository.ApplicationRepositories;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.context.annotation.Import;

@Import(ApplicationRepositories.class)
@SpringBootApplication(exclude = ElasticsearchDataAutoConfiguration.class)
public class ApplicationServices {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApplicationServices.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setBannerMode(Banner.Mode.CONSOLE);
        app.run(args);
    }
}
