package com.springboot.learning.sb3.producer;

import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface IAttributeDictionarySenderService<T> {

    /**
     * @param entity : the entity
     * @return flow of {@link AttributeDictionaryEntity}
     */
    Mono<T> send(@NotNull T entity);
}
