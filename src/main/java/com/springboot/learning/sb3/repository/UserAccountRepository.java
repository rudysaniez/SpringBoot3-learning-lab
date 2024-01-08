package com.springboot.learning.sb3.repository;

import com.springboot.learning.sb3.domain.UserAccountEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserAccountRepository extends CrudRepository<UserAccountEntity, Long> {

    Optional<UserAccountEntity> findByUsername(String username);
}
