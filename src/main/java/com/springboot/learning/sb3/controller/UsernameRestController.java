package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.domain.UsernameEntity;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequestMapping(value = "/v1")
@RestController
public class UsernameRestController {

    private final ReactiveOpensearchRepository opensearchRepository;

    private static final String IDX_TARGET = "username_v1";

    private static final Logger log = LoggerFactory.getLogger(UsernameRestController.class);

    public UsernameRestController(ReactiveOpensearchRepository opensearchRepository) {
        this.opensearchRepository = opensearchRepository;
    }

    /**
     * @param usernameEntity : the attribute
     * @return flow of {@link AttributeDictionaryEntity}
     */
    @PostMapping(value = "/usernames",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UsernameEntity>> save(@RequestBody UsernameEntity usernameEntity) {

        return opensearchRepository.save(IDX_TARGET, usernameEntity, UsernameEntity.class)
                .doOnError(t -> log.error(t.getMessage(), t))
                .onErrorResume(t -> Mono.empty())
                .map(entity -> new ResponseEntity<>(entity, HttpStatus.CREATED))
                .defaultIfEmpty(ResponseEntity.unprocessableEntity().build());
    }
}
