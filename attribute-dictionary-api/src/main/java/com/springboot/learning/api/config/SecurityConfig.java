package com.springboot.learning.api.config;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    public static final String ROLE_WRITER = "WRITER";
    public static final String ROLE_READER = "READER";

    private static final String ATTR_BASE_PATH = "/v1/attributes";
    private static final String ATTR_BASE_PATH_AND_MORE = ATTR_BASE_PATH.concat("/**");

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * @param http : the http security
     * @return {@link SecurityWebFilterChain}
     */
    @Bean
    SecurityWebFilterChain defaultSecurity(ServerHttpSecurity http) {

        http.authorizeExchange(exchange -> exchange
            .pathMatchers("/management/info", "management/health").permitAll()
            .pathMatchers(HttpMethod.GET, ATTR_BASE_PATH, ATTR_BASE_PATH_AND_MORE)
                .hasAnyRole(ROLE_READER, ROLE_WRITER)
            .pathMatchers(HttpMethod.POST, ATTR_BASE_PATH, ATTR_BASE_PATH_AND_MORE)
                .hasAnyRole(ROLE_WRITER)
            .pathMatchers(HttpMethod.DELETE, ATTR_BASE_PATH, ATTR_BASE_PATH_AND_MORE)
                .hasAnyRole(ROLE_WRITER)
            .anyExchange().authenticated())
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * <a href="https://www.baeldung.com/spring-security-5-reactive">...</a>
     * @param userSecurityAccessConfiguration : The user security access configuration
     * @return {@link MapReactiveUserDetailsService}
     */
    @Bean
    MapReactiveUserDetailsService userDetailsService(PropertiesConfig.UserSecurityAccessConfiguration userSecurityAccessConfiguration) {

        final List<UserDetails> userDetailsManager = userSecurityAccessConfiguration.users().stream()
                .map(user -> buildUserDetails(user.username(), user.password(), user.authorities()))
                .toList();

        log.info(" > List of users is {}.", userDetailsManager);

        return new MapReactiveUserDetailsService(userDetailsManager);
    }

    /**
     * @param username : the username
     * @param password : the password
     * @param roles : the roles
     * @return {@link UserDetails}
     */
    private UserDetails buildUserDetails(@NotNull String username,
                                         @NotNull String password,
                                         @NotNull List<String> roles) {

        final String pass = passwordEncoder().encode(password);
        return User.withUsername(username)
                .password(pass)
                .roles(roles.toArray(String[]::new))
                .build();
    }
}
