package com.springboot.learning.backend.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiBackendConfig {

    @Bean
    OpenAPI openAPI() {

        return new OpenAPI()
                .info(new Info().title("Backend attribute reactive API")
                        .description("Backend attribute reactive API")
                        .version("1")
                        .contact(new Contact()
                                .name("rudysaniez")
                                .url("https://github.com/adeo/pro-cust-xp-bonsai-backend-api")
                                .email("rudysaniez@adeo.com")));
    }
}
