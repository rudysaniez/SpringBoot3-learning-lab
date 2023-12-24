package com.adeo.springboot.learning.sb3;

import com.adeo.springboot.learning.sb3.config.PropertiesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(value = {PropertiesConfig.UserSecurityAccess.class,
										PropertiesConfig.Pagination.class
								})
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
