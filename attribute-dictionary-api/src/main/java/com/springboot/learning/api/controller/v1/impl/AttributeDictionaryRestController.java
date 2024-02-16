package com.springboot.learning.api.controller.v1.impl;

import com.springboot.learning.api.controller.v1.AttributeDictionary;
import com.springboot.learning.api.controller.v1.AttributeDictionaryAPI;
import com.springboot.learning.api.controller.v1.BulkResult;
import com.springboot.learning.api.controller.v1.Page;
import com.springboot.learning.api.mapper.v1.AttributeDictionaryMapper;
import com.springboot.learning.api.sender.service.AttributeDictionarySenderService;
import com.springboot.learning.service.contract.v1.impl.AttributeDictionaryService;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.List;

@ConditionalOnProperty(prefix = "service", name = "version", havingValue = "v1")
@RequestMapping(value = "/v1")
@RestController
public class AttributeDictionaryRestController implements AttributeDictionaryAPI {

    private final AttributeDictionaryService attributeDictionaryService;
    private final AttributeDictionarySenderService attributeDictionarySenderService;

    private static final AttributeDictionaryMapper mapper = Mappers.getMapper(AttributeDictionaryMapper.class);
    private static final Logger log = LoggerFactory.getLogger(AttributeDictionaryRestController.class);

    public AttributeDictionaryRestController(AttributeDictionaryService attributeDictionaryService,
                                             AttributeDictionarySenderService attributeDictionarySenderService) {

        this.attributeDictionaryService = attributeDictionaryService;
        this.attributeDictionarySenderService = attributeDictionarySenderService;
    }

    /**
     * @param id : the id
     * @return flow of {@link AttributeDictionary}
     */
    @Override
    public Mono<ResponseEntity<AttributeDictionary>> getAttributeById(@PathVariable(value = "id") String id) {

        log.info(" > Get attribute by identifier : {}.", id);

        return attributeDictionaryService.getAttributeById(id)
                .map(mapper::toModel)
                .doOnError(t -> log.error(t.getMessage(), t))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * @return flow of {@link AttributeDictionary}
     */
    @Override
    public Mono<ResponseEntity<Page<AttributeDictionary>>> getAttributesAsPage(int page, int size) {

        log.info(" > Get attributes as page, page={} and size={}.", page, size);

        return attributeDictionaryService.searchAsPage(page, size)
                .map(mapper::toPageModel)
                .doOnError(t -> log.error(t.getMessage(), t))
                .map(ResponseEntity::ok);
    }

    /**
     * @param q : the query as paramName=value (code=code01)
     * @return {@link AttributeDictionary}
     */
    @Override
    public Flux<AttributeDictionary> searchAttributes(@RequestParam(value = "q") String q) {

        final var query = q.split("=");
        final var queryParam = new AbstractMap.SimpleImmutableEntry<>(query[0], query[1]);

        log.info(" > Search an attribute with query is {}, key={}, value={}.",
                q, queryParam.getKey(), queryParam.getValue());

        return attributeDictionaryService.searchWithQueryPrefix(queryParam.getKey(), queryParam.getValue(), 10)
                .map(mapper::toModel)
                .doOnError(t -> log.error(t.getMessage(), t));
    }

    /**
     * @param attributeDictionary : the attribute
     * @return flow of {@link AttributeDictionary}
     */
    @Override
    public Mono<ResponseEntity<AttributeDictionary>> saveAttribute(@RequestBody AttributeDictionary attributeDictionary) {

        log.info(" > Save an attribute : {}.", attributeDictionary);

        return Mono.just(attributeDictionary)
                .map(mapper::toEntity)
                .flatMap(attributeDictionaryService::save)
                .map(mapper::toModel)
                .doOnError(t -> log.error(t.getMessage(), t))
                .map(entity -> new ResponseEntity<>(entity, HttpStatus.CREATED));
    }

    /**
     * @param attributeDictionary : the attribute
     * @return flow of {@link AttributeDictionary}
     */
    @Override
    public Mono<ResponseEntity<Void>> saveAttributeAsync(@RequestBody AttributeDictionary attributeDictionary) {

        log.info(" > Save an attribute asynchronously : {}.", attributeDictionary);

        return Mono.just(attributeDictionary)
                .map(mapper::toEntity)
                .flatMap(attributeDictionarySenderService::send)
                .doOnError(t -> log.error(t.getMessage(), t))
                .map(model -> new ResponseEntity<>(HttpStatus.ACCEPTED));
    }

    /**
     *
     * @param id : the identifier
     * @param attributeDictionary : the attribute
     * @return flow of {@link AttributeDictionary}
     */
    public Mono<ResponseEntity<AttributeDictionary>> updateAttribute(@PathVariable(value = "id") String id,
                                                            @RequestBody AttributeDictionary attributeDictionary) {

        log.info(" > Update attribute {} with this identifier {}.", attributeDictionary, id);

        return Mono.just(attributeDictionary)
                .map(mapper::toEntity)
                .flatMap(entity -> attributeDictionaryService.update(id, entity))
                .doOnError(t -> log.error(t.getMessage(), t))
                .map(mapper::toModel)
                .map(ResponseEntity::ok);
    }

    /**
     * @return {@link Integer}
     */
    @Override
    public Mono<ResponseEntity<Void>> deleteOneAttribute(@PathVariable(value = "id") String id) {

        log.info(" > Delete one attribute dictionary by {}.", id);

        return attributeDictionaryService.deleteOne(id)
                .doOnError(t -> log.error(t.getMessage(), t))
                .filter(result -> result.equals(200))
                .map(status -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * @return {@link Long}
     */
    public Mono<ResponseEntity<Long>> deleteAllAttributes() {

        log.info(" > Delete all attributes dictionary.");

        return attributeDictionaryService.deleteAll()
                .filter(count -> count > 0)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }
}
