package com.adeo.springboot.learning.sb3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;

@Configuration
class Start {

	@Bean
	@RestartScope
	@ServiceConnection
	PostgreSQLContainer<?> postgreSQLContainer() {
		return new PostgreSQLContainer("postgres:15.4-alpine")
				.withUsername("michael")
				.withPassword("jordan")
				.withDatabaseName("videoDatabase");
	}

	public static void main(String[] args) {
		SpringApplication.from(Application::main)
				.with(Start.class)
				.run(args);
	}
}
