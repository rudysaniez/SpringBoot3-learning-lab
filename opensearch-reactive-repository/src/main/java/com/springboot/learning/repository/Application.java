package com.springboot.learning.repository;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;

/**
 * <a href="https://github.com/opensearch-project/spring-data-opensearch/tree/main/spring-data-opensearch-examples/spring-boot-gradle">...</a>
 */
@SpringBootApplication(exclude = ElasticsearchDataAutoConfiguration.class)
public class Application {

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(Application.class);
		app.setWebApplicationType(WebApplicationType.NONE);
		app.setBannerMode(Banner.Mode.CONSOLE);
		app.run(args);
	}
}
