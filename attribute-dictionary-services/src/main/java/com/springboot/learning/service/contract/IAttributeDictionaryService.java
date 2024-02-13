package com.springboot.learning.service.contract;

import com.springboot.learning.repository.impl.ReactiveOpensearchRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAttributeDictionaryService<T> {

    /**
     * @return {@link String}
     */
    String getIndexName();

    /**
     * @param id : the identifier
     * @return @{@link T}
     */
    Mono<T> getAttributeById(String id);

    /**
     * @param page : the page number
     * @param size : the page size
     * @return page of {@link T}
     */
    Mono<ReactiveOpensearchRepository.Page<T>> searchAsPage(int page, int size);

    /**
     * @param field : the field name
     * @param value : the value of field name
     * @param limit : the limit
     * @return {@link T}
     */
    Flux<T> searchWithQueryPrefix(String field, String value, int limit);

    /**
     * @param limit : the limit
     * @return {@link T}
     */
    Flux<T> searchNoPredicateButLimited(int limit);

    /**
     *
     * @param entity : the entity
     * @return flow of {@link T}
     */
    Mono<T> save(T entity);

    /**
     * @param entities : the entities
     * @return {@link ReactiveOpensearchRepository.CrudResult}
     */
    Flux<ReactiveOpensearchRepository.CrudResult> bulk(List<T> entities);

    /**
     * @param id : the identifier
     * @param entity : the entity
     * @return {@link T}
     */
    Mono<T> update(String id, T entity);

    /**
     * @param id : the identifier
     * @return {@link Integer}
     */
    Mono<Integer> deleteOne(String id);

    /**
     * @return {@link ReactiveOpensearchRepository.CrudResult}
     */
    Mono<Long> deleteAll();
}
