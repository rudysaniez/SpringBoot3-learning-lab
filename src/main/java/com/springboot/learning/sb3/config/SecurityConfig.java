package com.springboot.learning.sb3.config;

import com.springboot.learning.sb3.domain.UserAccountEntity;
import com.springboot.learning.sb3.repository.UserAccountRepository;
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
import java.util.Optional;

@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    public static final String ROLE_WRITER = "WRITER";
    public static final String ROLE_READER = "READER";

    private final UserAccountRepository userAccountRepository;

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Bean
    SecurityWebFilterChain defaultSecurity(ServerHttpSecurity http) {

        http.authorizeExchange(exchange -> exchange
            .pathMatchers("/management/info", "management/health").permitAll()
            .pathMatchers(HttpMethod.GET, "/videos")
                .hasAnyRole(ROLE_READER)
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
     *
     * @param userSecurityAccessConfiguration
     * @return
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
     * @param username
     * @param password
     * @param roles
     * @return
     */
    private UserDetails buildUserDetails(String username,
                                         String password,
                                         List<String> roles) {

        final Optional<UserAccountEntity> entity = userAccountRepository.findByUsername(username).blockOptional();
        return entity.map(UserAccountEntity::asUser).orElseGet(() -> {

            final String pass = passwordEncoder().encode(password);
            final var entityCreated = userAccountRepository.save(new UserAccountEntity(null,
                    username,
                    pass,
                    roles))
                    .block();

            return User.withUsername(entityCreated.username())
                    .password(entityCreated.password())
                    .roles(entityCreated.authorities().toArray(String[]::new))
                    .build();
        });
    }
}
