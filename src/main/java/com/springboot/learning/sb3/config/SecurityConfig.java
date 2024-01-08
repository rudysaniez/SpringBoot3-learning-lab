package com.springboot.learning.sb3.config;

import com.springboot.learning.sb3.domain.UserAccountEntity;
import com.springboot.learning.sb3.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Optional;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    public static final String ROLE_WRITER = "WRITER";
    public static final String ROLE_READER = "READER";
    public static final String ROLE_ADMIN = "ADMIN";

    private final UserAccountRepository userAccountRepository;

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * @param http : the http security
     * @return {@link SecurityFilterChain}
     */
    @Bean
    SecurityFilterChain defaultSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/", "/web", "/web/videos")
                    .hasAnyRole(ROLE_READER)

                .requestMatchers(HttpMethod.GET, "/videos")
                    .hasAnyRole(ROLE_READER)

                .requestMatchers(HttpMethod.GET, "/videos/:search", "/videos/:count")
                    .authenticated()

                .requestMatchers("/management/info", "management/health")
                    .permitAll()

                .requestMatchers(HttpMethod.POST, "/videos")
                    .hasAnyRole(ROLE_WRITER)

                .requestMatchers(HttpMethod.POST, "/web/videos/:create")
                    .hasAnyRole(ROLE_WRITER)

                .requestMatchers(HttpMethod.POST, "/web/videos/:search")
                    .authenticated()

                .requestMatchers(HttpMethod.DELETE, "/videos")
                    .hasAnyRole(ROLE_WRITER)

                .anyRequest().denyAll()
            )
            .formLogin(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults())
            .csrf(CsrfConfigurer::disable);

        return http.build();
    }

    /**
     * @return {@link PasswordEncoder}
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * <a href="https://www.baeldung.com/java-spring-fix-403-error">How to solve 403 error in Spring</a>
     * @param userSecurityAccessConfiguration : the user security access
     * @return {@link UserDetailsService}
     */
    @Bean
    UserDetailsService userDetailsService(PropertiesConfig.UserSecurityAccessConfiguration userSecurityAccessConfiguration) {

        final UserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();

        userSecurityAccessConfiguration.users().stream()
                .map(user -> buildUserDetails(user.username(), user.password(), user.authorities()))
                .forEach(userDetailsManager::createUser);

        return userDetailsManager;
    }

    /**
     * @param username : the username
     * @param password : the password
     * @param roles : the roles
     * @return {@link UserDetails}
     */
    private UserDetails buildUserDetails(String username,
                                         String password,
                                         List<String> roles) {

        log.info(" > Create the user details for username {}, and authorities are {}.", username, roles);

        final Optional<UserAccountEntity> entity = userAccountRepository.findByUsername(username);
        return entity.map(UserAccountEntity::asUser).orElseGet(() -> {

            final String pass = passwordEncoder().encode(password);
            final var entityCreated = userAccountRepository.save(new UserAccountEntity(null,
                    username,
                    pass,
                    roles));

            return User.withUsername(entityCreated.username())
                    .password(entityCreated.password())
                    .roles(entityCreated.authorities().toArray(String[]::new))
                    .build();
        });
    }
}
