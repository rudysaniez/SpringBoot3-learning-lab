package com.springboot.learning.sb3;

import com.springboot.learning.sb3.config.PropertiesConfig;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.hateoas.config.EnableHypermediaSupport;

/**
 * <a href="https://github.com/opensearch-project/spring-data-opensearch/tree/main/spring-data-opensearch-examples/spring-boot-gradle">...</a>
 */
//@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableConfigurationProperties(value = {PropertiesConfig.UserSecurityAccessConfiguration.class,
										PropertiesConfig.Pagination.class
								})
@SpringBootApplication(exclude = ElasticsearchDataAutoConfiguration.class)
public class Application {

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(Application.class);
		app.setWebApplicationType(WebApplicationType.REACTIVE);
		app.setBannerMode(Banner.Mode.CONSOLE);
		app.run(args);
	}
}
