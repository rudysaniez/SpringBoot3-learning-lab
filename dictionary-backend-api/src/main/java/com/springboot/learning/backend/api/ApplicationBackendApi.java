package com.springboot.learning.backend.api;

import com.springboot.learning.backend.api.config.PropertiesBackendConfig;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(value = {PropertiesBackendConfig.Pagination.class,
										PropertiesBackendConfig.Microservices.class})
@SpringBootApplication
public class ApplicationBackendApi {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ApplicationBackendApi.class);
		app.setWebApplicationType(WebApplicationType.REACTIVE);
		app.setBannerMode(Banner.Mode.CONSOLE);
		app.run(args);
	}
}
