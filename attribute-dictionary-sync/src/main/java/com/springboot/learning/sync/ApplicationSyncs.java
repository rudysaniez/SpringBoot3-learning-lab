package com.springboot.learning.sync;

import com.springboot.learning.service.ApplicationServices;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(value = ApplicationServices.class)
@SpringBootApplication
public class ApplicationSyncs {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApplicationSyncs.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setBannerMode(Banner.Mode.CONSOLE);
        app.run(args);
    }
}
