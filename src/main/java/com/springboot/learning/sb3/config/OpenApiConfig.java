package com.springboot.learning.sb3.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {

        return new OpenAPI()
                .info(new Info().title("Attribute dictionary reactive API")
                        .description("Description...")
                        .version("1")
                        .contact(new Contact()
                                .name("Contact name")
                                .url("Contact URL")
                                .email("Contact email"))
                        .termsOfService("Terms of service")
                        .license(new License()
                                .name("Licence")
                                .url("Licence URL")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentation")
                        .url("Documentation URL"));
    }
}
