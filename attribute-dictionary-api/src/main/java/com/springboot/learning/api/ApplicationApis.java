package com.springboot.learning.api;

import com.springboot.learning.api.config.PropertiesConfig;
import com.springboot.learning.service.ApplicationServices;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * <a href="https://github.com/opensearch-project/spring-data-opensearch/tree/main/spring-data-opensearch-examples/spring-boot-gradle">...</a>
 */
@Import(value = ApplicationServices.class)
@EnableConfigurationProperties(value = {PropertiesConfig.UserSecurityAccessConfiguration.class,
										PropertiesConfig.Pagination.class,
										PropertiesConfig.ApplicationOpensearchConfiguration.class})
@SpringBootApplication(exclude = ElasticsearchDataAutoConfiguration.class)
public class ApplicationApis {

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(ApplicationApis.class);
		app.setWebApplicationType(WebApplicationType.REACTIVE);
		app.setBannerMode(Banner.Mode.CONSOLE);
		app.run(args);
	}
}
