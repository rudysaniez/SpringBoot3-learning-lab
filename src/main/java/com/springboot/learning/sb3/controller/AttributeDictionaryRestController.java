package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.core.CountRequest;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.List;

@RequestMapping(value = "/v1")
@RestController
public class AttributeDictionaryRestController {

    private final ReactiveOpensearchRepository opensearchRepository;

    private static final String IDX_TARGET = "attributs_dictionnary_v1";

    private static final Logger log = LoggerFactory.getLogger(AttributeDictionaryRestController.class);

    public AttributeDictionaryRestController(ReactiveOpensearchRepository opensearchRepository) {
        this.opensearchRepository = opensearchRepository;
    }

    /**
     * @param id : the id
     * @return flow of {@link AttributeDictionaryEntity}
     */
    @GetMapping(value = "/attributes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AttributeDictionaryEntity>> getById(@PathVariable(value = "id") String id) {

        log.info(" > Get attributes V1 by id is {}", id);

        return opensearchRepository.getById(IDX_TARGET, id, AttributeDictionaryEntity.class)
                .doOnError(t -> log.error(t.getMessage(), t))
                .onErrorResume(t -> Mono.empty())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * @return flow of {@link AttributeDictionaryEntity}
     */
    @GetMapping(value = "/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReactiveOpensearchRepository.Page<AttributeDictionaryEntity>>> getAll() {

        log.info(" > Get attributes as page.");

        final var search = SearchSourceBuilder.searchSource().from(0).size(5);
        final var request = new SearchRequest(new String[]{IDX_TARGET}, search);
        return opensearchRepository.searchAsPage(request, new CountRequest(IDX_TARGET), AttributeDictionaryEntity.class)
                .doOnError(t -> log.error(t.getMessage(), t))
                .onErrorResume(t -> Mono.empty())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * @param q : the query as paramName=value (code=code01)
     * @return {@link AttributeDictionaryEntity}
     */
    @GetMapping(value = "/attributes/:search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<AttributeDictionaryEntity> search(@RequestParam(value = "q") String q) {

        final var query = q.split("=");
        final var queryParam = new AbstractMap.SimpleImmutableEntry<>(query[0], query[1]);

        final SearchSourceBuilder search = SearchSourceBuilder.searchSource()
                .query(QueryBuilders.prefixQuery(queryParam.getKey(), queryParam.getValue()))
                .from(0)
                .size(5);
        final var request = new SearchRequest(new String[]{IDX_TARGET}, search);
        return opensearchRepository.search(request, AttributeDictionaryEntity.class)
                .doOnError(t -> log.error(t.getMessage(), t))
                .onErrorResume(t -> Flux.empty());
    }

    /**
     * @param attr : the attribute
     * @return flow of {@link AttributeDictionaryEntity}
     */
    @PostMapping(value = "/attributes",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AttributeDictionaryEntity>> save(@RequestBody AttributeDictionaryEntity attr) {

        return opensearchRepository.save(IDX_TARGET, attr, AttributeDictionaryEntity.class)
                .doOnError(t -> log.error(t.getMessage(), t))
                .onErrorResume(t -> Mono.empty())
                .map(entity -> new ResponseEntity<>(entity, HttpStatus.CREATED))
                .defaultIfEmpty(ResponseEntity.unprocessableEntity().build());
    }

    /**
     * @param attrs : attributes
     * @return flow of list of {@link ReactiveOpensearchRepository.CrudResult}
     */
    @PostMapping(value = "/attributes/:bulk",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<ReactiveOpensearchRepository.CrudResult>>> saveAll(@RequestBody List<AttributeDictionaryEntity> attrs) {

        return opensearchRepository.bulk(attrs, IDX_TARGET)
                .doOnError(t -> log.error(t.getMessage(), t))
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(t -> Mono.just(ResponseEntity.noContent().build()));
    }

    /**
     * @return {@link Integer}
     */
    @DeleteMapping(value = "/attributes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> deleteOne(@PathVariable(value = "id") String id) {

        return opensearchRepository.delete(IDX_TARGET, id)
                .doOnError(t -> log.error(t.getMessage(), t))
                .onErrorResume(t -> Mono.empty())
                .filter(result -> result.equals(200))
                .map(status -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * @return {@link ReactiveOpensearchRepository.CrudResult}
     */
    @DeleteMapping(value = "/attributes/:empty", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<ReactiveOpensearchRepository.CrudResult>>> deleteAll() {

        return opensearchRepository.search(new SearchRequest(IDX_TARGET), AttributeDictionaryEntity.class)
                .doOnError(t -> log.error(t.getMessage(), t))
                .map(AttributeDictionaryEntity::getId)
                .collectList()
                .flatMap(ids -> opensearchRepository.deleteAll(IDX_TARGET, ids))
                .map(ResponseEntity::ok)
                .onErrorResume(t -> Mono.just(ResponseEntity.noContent().build()));
    }
}
