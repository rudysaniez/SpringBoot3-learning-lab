package com.springboot.learning.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiDictionaryConfig {

    @Bean
    OpenAPI openAPI() {

        return new OpenAPI()
                .info(new Info().title("Dictionary reactive API")
                        .description("The dictionary reactive API")
                        .version("1")
                        .contact(new Contact()
                                .name("rudysaniez")
                                .url("https://github.com/adeo/pro-cust-xp-bonsai-attribute-dictionary-api")
                                .email("rudysaniez@adeo.com")));
    }
}
