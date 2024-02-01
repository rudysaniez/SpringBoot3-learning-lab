package com.springboot.learning.sb3.service;

import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AttributeDictionaryService {

    private final ReactiveOpensearchRepository repository;

    private static final String IDX_TARGET = "attributs_dictionnary_v1";

    public AttributeDictionaryService(ReactiveOpensearchRepository repository) {
        this.repository = repository;
    }

    /**
     *
     * @param entity
     * @return flow of {@link AttributeDictionaryEntity}
     */
    public Mono<AttributeDictionaryEntity> save(AttributeDictionaryEntity entity) {
        return repository.save(IDX_TARGET, entity, AttributeDictionaryEntity.class);
    }
}
