package com.springboot.learning.backend.api.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SecurityScheme(
    name = "security_auth", type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(
        authorizationCode = @OAuthFlow(
            authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}",
            tokenUrl = "${springdoc.oAuthFlow.tokenUrl}",
            scopes = {
                @OAuthScope(name = "attribute:read", description = "read scope"),
                @OAuthScope(name = "attribute:write", description = "write scope")
            }
        )
    )
)
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
