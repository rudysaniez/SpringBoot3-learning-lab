package com.springboot.learning.sb3.repository;

import com.springboot.learning.sb3.domain.UserAccountEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface UserAccountRepository extends ReactiveCrudRepository<UserAccountEntity, Long> {

    Mono<UserAccountEntity> findByUsername(String username);
}
