package com.adeo.springboot.learning.sb3.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Table("user_account")
public record UserAccountEntity(@Id Long id, String username, String password, List<String> authorities) {

    /**
     * @return {@link UserDetails}
     */
    public UserDetails asUser() {
        return User.withUsername(username)
                .password(password)
                .roles(authorities.toArray(String[]::new))
                .build();
    }
}
