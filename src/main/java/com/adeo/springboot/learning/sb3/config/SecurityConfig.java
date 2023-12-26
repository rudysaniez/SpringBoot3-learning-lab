package com.adeo.springboot.learning.sb3.config;

import com.adeo.springboot.learning.sb3.domain.UserAccountEntity;
import com.adeo.springboot.learning.sb3.repository.UserAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.GrantedAuthority;
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
import java.util.stream.Stream;

@Configuration
public class SecurityConfig {

    public static final String ROLE_WRITER = "WRITER";
    public static final String ROLE_READER = "READER";
    public static final String ROLE_ADMIN = "ADMIN";

    private final UserAccountRepository userAccountRepository;

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
                .requestMatchers(HttpMethod.GET, "/videos", "/videos/:count", "/videos/:search")
                .hasAnyRole(ROLE_READER, ROLE_WRITER, ROLE_ADMIN)

                .requestMatchers("/management/info", "management/health")
                .permitAll()

                .requestMatchers(HttpMethod.POST, "/videos")
                .hasAnyRole(ROLE_WRITER)

                .requestMatchers(HttpMethod.DELETE, "/videos")
                .hasAnyRole(ROLE_WRITER, ROLE_ADMIN)

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
     * @param userSecurityAccess : the user security access
     * @return {@link UserDetailsService}
     */
    @Bean
    UserDetailsService userDetailsService(PropertiesConfig.UserSecurityAccess userSecurityAccess) {

        final UserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();

        final var adminUserDetails = buildUserDetails(userSecurityAccess.admin(),
                userSecurityAccess.adminPassword(),
                List.of(ROLE_READER, ROLE_ADMIN));
        userDetailsManager.createUser(adminUserDetails);

        final var simpleUserDetails = buildUserDetails(userSecurityAccess.user(),
                userSecurityAccess.userPassword(),
                List.of(ROLE_READER));
        userDetailsManager.createUser(simpleUserDetails);

        final var writerUserDetails = buildUserDetails(userSecurityAccess.writer(),
                userSecurityAccess.writerPassword(),
                List.of(ROLE_WRITER, ROLE_READER));
        userDetailsManager.createUser(writerUserDetails);

        /*
        lambda is already created with a temporary password...
        The lambda user has only a READER role.
         */
        Optional<UserAccountEntity> lambdaEntity = userAccountRepository.findByUsername("lambda");
        if(lambdaEntity.isPresent()) {

            final var pass = passwordEncoder().encode(lambdaEntity.get().password());
            UserAccountEntity entityUpdated = new UserAccountEntity(lambdaEntity.get().id(),
                    lambdaEntity.get().username(),
                    pass,
                    lambdaEntity.get().authorities());

            final var lambdaUpdated = userAccountRepository.save(entityUpdated);

            UserDetails lambdaUserDetails = User.withUsername(lambdaUpdated.username())
                    .password(lambdaUpdated.password())
                    .roles(lambdaUpdated.authorities().toArray(String[]::new))
                    .build();

            userDetailsManager.createUser(lambdaUserDetails);
        }

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
