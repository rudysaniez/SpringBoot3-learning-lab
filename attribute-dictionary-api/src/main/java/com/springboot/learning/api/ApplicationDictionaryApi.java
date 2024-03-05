package com.springboot.learning.api;

import com.springboot.learning.api.config.PropertiesDictionaryConfig;
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
@EnableConfigurationProperties(value = {PropertiesDictionaryConfig.UserSecurityAccessConfiguration.class,
										PropertiesDictionaryConfig.Pagination.class})
@SpringBootApplication
public class ApplicationDictionaryApi {

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(ApplicationDictionaryApi.class);
		app.setWebApplicationType(WebApplicationType.REACTIVE);
		app.setBannerMode(Banner.Mode.CONSOLE);
		app.run(args);
	}
}
