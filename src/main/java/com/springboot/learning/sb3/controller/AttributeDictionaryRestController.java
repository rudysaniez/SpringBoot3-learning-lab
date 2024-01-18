package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class AttributeDictionaryRestController {

    private final ReactiveOpensearchRepository opensearchRepository;

    private static final String IDX = "attributs_dictionnary_v1";

    public AttributeDictionaryRestController(ReactiveOpensearchRepository opensearchRepository) {
        this.opensearchRepository = opensearchRepository;
    }

    /**
     * @param code : the code
     * @return flow of {@link AttributeDictionaryEntity}
     */
    @GetMapping(value = "/attributes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AttributeDictionaryEntity>> getById(@PathVariable(value = "code") String code) {

        final var request = new SearchRequest(IDX);
        final var builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchPhraseQuery("code", code));
        request.source(builder);

        return opensearchRepository.findOne("code", code, AttributeDictionaryEntity.class)
                .map(ResponseEntity::ok);
    }

    /**
     * @return flow of {@link AttributeDictionaryEntity}
     */
    @GetMapping(value = "/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<AttributeDictionaryEntity> getAll() {
        return opensearchRepository.search(new SearchRequest(IDX), AttributeDictionaryEntity.class);
    }

    /**
     * @param attr : the attribute
     * @return flow of {@link AttributeDictionaryEntity}
     */
    @PostMapping(value = "/attributes",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AttributeDictionaryEntity>> save(@RequestBody AttributeDictionaryEntity attr) {

        return opensearchRepository.save(IDX, attr, AttributeDictionaryEntity.class)
                .map(ResponseEntity::ok);
    }
}
