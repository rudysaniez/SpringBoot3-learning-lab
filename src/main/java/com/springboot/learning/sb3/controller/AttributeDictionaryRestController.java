package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.controller.contract.AttributeDictionary;
import com.springboot.learning.sb3.controller.contract.AttributeDictionaryAPI;
import com.springboot.learning.sb3.controller.contract.BulkResult;
import com.springboot.learning.sb3.controller.contract.Page;
import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.mapper.v1.AttributeDictionaryMapper;
import com.springboot.learning.sb3.producer.v1.AttributeDictionarySenderService;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import com.springboot.learning.sb3.service.v1.AttributeDictionaryService;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.List;

@RequestMapping(value = "/v1")
@RestController
public class AttributeDictionaryRestController implements AttributeDictionaryAPI {

    private final AttributeDictionarySenderService attributeSenderService;
    private final AttributeDictionaryService attributeDictionaryService;

    private static final AttributeDictionaryMapper mapper = Mappers.getMapper(AttributeDictionaryMapper.class);
    private static final Logger log = LoggerFactory.getLogger(AttributeDictionaryRestController.class);

    public AttributeDictionaryRestController(AttributeDictionarySenderService attributeSenderService,
                                             AttributeDictionaryService attributeDictionaryService) {

        this.attributeSenderService = attributeSenderService;
        this.attributeDictionaryService = attributeDictionaryService;
    }

    /**
     * @param id : the id
     * @return flow of {@link AttributeDictionaryEntity}
     */
    @Override
    public Mono<ResponseEntity<AttributeDictionary>> getAttributeById(@PathVariable(value = "id") String id) {

        log.info(" > Get attribute by identifier : {}.", id);

        return attributeDictionaryService.getAttributeById(id)
                .map(mapper::toModel)
                .doOnError(t -> log.error(t.getMessage(), t))
                .onErrorResume(t -> Mono.empty())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * @return flow of {@link AttributeDictionaryEntity}
     */
    @Override
    public Mono<ResponseEntity<Page<AttributeDictionary>>> getAttributesAsPage(int page, int size) {

        log.info(" > Get attributes as page, page={} and size={}.", page, size);

        return attributeDictionaryService.searchAsPage(page, size)
                .map(mapper::toPageModel)
                .doOnError(t -> log.error(t.getMessage(), t))
                .onErrorResume(t -> Mono.empty())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * @param q : the query as paramName=value (code=code01)
     * @return {@link AttributeDictionaryEntity}
     */
    @Override
    public Flux<AttributeDictionary> searchAttributes(@RequestParam(value = "q") String q) {

        log.info(" > Search an attribute with query is {}.", q);

        final var query = q.split("=");
        final var queryParam = new AbstractMap.SimpleImmutableEntry<>(query[0], query[1]);

        return attributeDictionaryService.searchWithQueryPrefix(queryParam.getKey(), queryParam.getValue(), 10)
                .map(mapper::toModel)
                .doOnError(t -> log.error(t.getMessage(), t))
                .onErrorResume(t -> Flux.empty());
    }

    /**
     * @param attributeDictionary : the attribute
     * @return flow of {@link AttributeDictionaryEntity}
     */
    @Override
    public Mono<ResponseEntity<AttributeDictionary>> saveAttribute(@RequestBody AttributeDictionary attributeDictionary) {

        log.info(" > Save an attribute : {}.", attributeDictionary);

        return Mono.just(attributeDictionary)
                .map(mapper::toEntity)
                .flatMap(attributeDictionaryService::save)
                .map(mapper::toModel)
                .doOnError(t -> log.error(t.getMessage(), t))
                .onErrorResume(t -> Mono.empty())
                .map(entity -> new ResponseEntity<>(entity, HttpStatus.CREATED))
                .defaultIfEmpty(ResponseEntity.unprocessableEntity().build());
    }

    /**
     * @param attributeDictionary : the attribute
     * @return flow of {@link AttributeDictionaryEntity}
     */
    @Override
    public Mono<ResponseEntity<Void>> saveAttributeAsync(@RequestBody AttributeDictionary attributeDictionary) {

        log.info(" > Save an attribute asynchronously : {}.", attributeDictionary);

        return Mono.just(attributeDictionary)
                .map(mapper::toEntity)
                .flatMap(attributeSenderService::send)
                .doOnError(t -> log.error(t.getMessage(), t))
                .onErrorResume(t -> Mono.empty())
                .map(model -> new ResponseEntity<Void>(HttpStatus.ACCEPTED))
                .defaultIfEmpty(ResponseEntity.unprocessableEntity().build());
    }

    /**
     * @param attributeDictionaries : attributes
     * @return flow of list of {@link ReactiveOpensearchRepository.CrudResult}
     */
    @Override
    public Mono<ResponseEntity<List<BulkResult>>> bulkAttributes(@RequestBody List<AttributeDictionary> attributeDictionaries) {

        log.info(" > Bulk attributes : {}.", attributeDictionaries);

        return Mono.just(attributeDictionaries)
                .map(mapper::toEntityList)
                .flatMapMany(attributeDictionaryService::bulk)
                .map(mapper::toBulkResultModel)
                .doOnError(t -> log.error(t.getMessage(), t))
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(t -> Mono.just(ResponseEntity.unprocessableEntity().build()));
    }

    /**
     * @param attributeDictionaries : attributes
     * @return flow of list of {@link ReactiveOpensearchRepository.CrudResult}
     */
    @Override
    public Mono<ResponseEntity<Void>> bulkAttributesAsync(@RequestBody List<AttributeDictionary> attributeDictionaries) {

        log.info(" > Bulk attributes asynchronously : {}.", attributeDictionaries);

        return Flux.fromIterable(attributeDictionaries)
                .map(mapper::toEntity)
                .flatMap(attributeSenderService::send)
                .doOnError(t -> log.error(t.getMessage(), t))
                .collectList()
                .map(entities -> new ResponseEntity<Void>(HttpStatus.ACCEPTED))
                .onErrorResume(t -> Mono.just(ResponseEntity.unprocessableEntity().build()));
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
                .onErrorResume(t -> Mono.empty())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.unprocessableEntity().build());
    }

    /**
     * @return {@link Integer}
     */
    @Override
    public Mono<ResponseEntity<Void>> deleteOneAttribute(@PathVariable(value = "id") String id) {

        return attributeDictionaryService.deleteOne(id)
                .doOnError(t -> log.error(t.getMessage(), t))
                .onErrorResume(t -> Mono.empty())
                .filter(result -> result.equals(200))
                .map(status -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * @return {@link ReactiveOpensearchRepository.CrudResult}
     */
    public Mono<ResponseEntity<List<BulkResult>>> deleteAllAttributes() {

        return attributeDictionaryService.searchNoPredicateButLimited(1000)
                .doOnError(t -> log.error(t.getMessage(), t))
                .map(AttributeDictionaryEntity::id)
                .collectList()
                .flatMap(attributeDictionaryService::deleteAll)
                .map(mapper::toBulkResultModels)
                .map(ResponseEntity::ok)
                .onErrorResume(t -> Mono.just(ResponseEntity.noContent().build()));
    }
}
