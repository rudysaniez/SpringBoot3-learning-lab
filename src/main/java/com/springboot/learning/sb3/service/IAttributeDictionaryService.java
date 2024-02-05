package com.springboot.learning.sb3.service;

import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAttributeDictionaryService {

    /**
     * @return {@link String}
     */
    String getIndexTarget();

    /**
     * @param id : the identifier
     * @return @{@link AttributeDictionaryEntity}
     */
    Mono<AttributeDictionaryEntity> getAttributeById(String id);

    /**
     * @param page : the page number
     * @param size : the page size
     * @return page of {@link AttributeDictionaryEntity}
     */
    Mono<ReactiveOpensearchRepository.Page<AttributeDictionaryEntity>> searchAsPage(int page, int size);

    /**
     * @param field : the field name
     * @param value : the value of field name
     * @param limit : the limit
     * @return {@link AttributeDictionaryEntity}
     */
    Flux<AttributeDictionaryEntity> searchWithQueryPrefix(String field, String value, int limit);

    /**
     * @param limit : the limit
     * @return {@link AttributeDictionaryEntity}
     */
    Flux<AttributeDictionaryEntity> searchNoPredicateButLimited(int limit);

    /**
     *
     * @param entity : the entity
     * @return flow of {@link AttributeDictionaryEntity}
     */
    Mono<AttributeDictionaryEntity> save(AttributeDictionaryEntity entity);

    /**
     * @param entities : the entities
     * @return {@link ReactiveOpensearchRepository.CrudResult}
     */
    Flux<ReactiveOpensearchRepository.CrudResult> bulk(List<AttributeDictionaryEntity> entities);

    /**
     * @param id : the identifier
     * @param entity : the entity
     * @return {@link AttributeDictionaryEntity}
     */
    Mono<AttributeDictionaryEntity> update(String id, AttributeDictionaryEntity entity);

    /**
     * @param id : the identifier
     * @return {@link Integer}
     */
    Mono<Integer> deleteOne(String id);

    /**
     * @param ids : the identifiers
     * @return {@link ReactiveOpensearchRepository.CrudResult}
     */
    Mono<List<ReactiveOpensearchRepository.CrudResult>> deleteAll(List<String> ids);
}
