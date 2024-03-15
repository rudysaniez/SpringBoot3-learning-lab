package com.springboot.learning.backend.api.config;

import com.springboot.learning.backend.api.Management;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@EnableWebFluxSecurity
@Configuration
public class SecurityBackendConfig {

    static final String WRITE = "SCOPE_attribute:write";
    static final String READ = "SCOPE_attribute:read";
    static final String[] AUTHORITIES_READ_WRITE = new String[]{READ, WRITE};

    private static final Logger log = LoggerFactory.getLogger(SecurityBackendConfig.class);

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        log.info(" > The security is disabled.");

        http.cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(auth -> auth
                .matchers(EndpointRequest.to(Management.INFO,Management.HEALTH)).permitAll()
                .pathMatchers("/openapi/**").permitAll()
                .pathMatchers("/webjars/**").permitAll()
                .pathMatchers("/dictionary/attributes/**").hasAnyAuthority(AUTHORITIES_READ_WRITE)
                .anyExchange().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * @return @{@link CorsConfigurationSource}
     */
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setMaxAge(8000L);
        configuration.setAllowedMethods(Arrays.asList("GET","PUT","POST","DELETE","PATCH"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
