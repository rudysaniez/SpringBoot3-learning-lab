package com.springboot.learning.eureka.server.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
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

@Configuration
public class EurekaSecurityConfig {

    private final String username;
    private final String password;

    @Autowired
    public EurekaSecurityConfig(@Value("${security.eureka-username}") String username,
                                @Value("${security.eureka-password}") String password) {

        this.username = username;
        this.password = password;
    }

    /**
     *
     * @param http : the http security
     * @return {@link SecurityFilterChain}
     */
    @Bean
    SecurityFilterChain defaultSecurity(HttpSecurity http) throws Exception {

        http.csrf(CsrfConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * @return {@link UserDetailsService}
     */
    @Bean
    UserDetailsService userDetailsService() {

        final UserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();

        var user = buildUserDetails(username, password, List.of("USER"));
        userDetailsManager.createUser(user);

        return userDetailsManager;
    }

    /**
     * @return {@link PasswordEncoder}
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
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
