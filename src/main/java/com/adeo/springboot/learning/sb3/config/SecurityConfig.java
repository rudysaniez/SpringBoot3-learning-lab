package com.adeo.springboot.learning.sb3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String ROLE_WRITER = "WRITER";
    private static final String ROLE_READER = "READER";

    /**
     * @param http : the http security
     * @return {@link SecurityFilterChain}
     */
    @Bean
    SecurityFilterChain defaultSecurity(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/videos", "/videos/:count", "/videos/:search")
                .hasAnyRole(ROLE_READER, ROLE_WRITER)

                .requestMatchers("/management/info", "management/health")
                .permitAll()

                .requestMatchers(HttpMethod.POST, "/videos")
                .hasAnyRole(ROLE_WRITER)

                .requestMatchers(HttpMethod.DELETE, "/videos")
                .hasAnyRole(ROLE_WRITER)

                .anyRequest()
                .denyAll()
            )
            .formLogin(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults());

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
     * @param userSecurityAccess : the user security access
     * @return {@link UserDetailsService}
     */
    @Bean
    UserDetailsService userDetailsService(PropertiesConfig.UserSecurityAccess userSecurityAccess) {

        UserDetailsManager manager = new InMemoryUserDetailsManager();

        /*
        Admin
         */
        var passAdmin = passwordEncoder().encode(userSecurityAccess.adminPassword());
        var userAdmin = User.withUsername(userSecurityAccess.admin())
               .password(passAdmin)
                .roles(ROLE_WRITER, ROLE_READER)
               .build();
        manager.createUser(userAdmin);

        /*
        User
         */
        var passUser = passwordEncoder().encode(userSecurityAccess.userPassword());
        var user = User.withUsername(userSecurityAccess.user())
                .password(passUser)
                .roles(ROLE_READER)
                .build();
        manager.createUser(user);

        /*
        Writer
         */
        var passWriter = passwordEncoder().encode(userSecurityAccess.writerPassword());
        var writer = User.withUsername(userSecurityAccess.writer())
                .password(passWriter)
                .roles(ROLE_WRITER, ROLE_READER)
                .build();
        manager.createUser(writer);

        return manager;
    }
}
