package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.controller.contract.AttributeDictionary;
import com.springboot.learning.sb3.controller.contract.AttributeDictionaryAPI;
import com.springboot.learning.sb3.controller.contract.BulkResult;
import com.springboot.learning.sb3.controller.contract.Page;
import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.mapper.AttributeMapper;
import com.springboot.learning.sb3.producer.AttributeSenderService;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import org.mapstruct.factory.Mappers;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.core.CountRequest;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
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

    private final ReactiveOpensearchRepository opensearchRepository;
    private final AttributeSenderService attributeSenderService;

    public static final String IDX_TARGET = "attributs_dictionnary_v1";
    public static final String BINDING_TARGET = "attributeDictionarySyncEventConsume-out-0";

    private static final AttributeMapper mapper = Mappers.getMapper(AttributeMapper.class);
    private static final Logger log = LoggerFactory.getLogger(AttributeDictionaryRestController.class);

    public AttributeDictionaryRestController(ReactiveOpensearchRepository opensearchRepository,
                                             AttributeSenderService attributeSenderService) {

        this.opensearchRepository = opensearchRepository;
        this.attributeSenderService = attributeSenderService;
    }

    /**
     * @param id : the id
     * @return flow of {@link AttributeDictionaryEntity}
     */
    @Override
    public Mono<ResponseEntity<AttributeDictionary>> getAttributeById(@PathVariable(value = "id") String id) {

        log.info(" > Get attribute by identifier : {}.", id);

        return opensearchRepository.getById(IDX_TARGET, id, AttributeDictionaryEntity.class)
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
    public Mono<ResponseEntity<Page<AttributeDictionary>>> getAllAttributes() {

        log.info(" > Get attributes as page.");

        final var search = SearchSourceBuilder.searchSource().from(0).size(5);
        final var request = new SearchRequest(new String[]{IDX_TARGET}, search);
        return opensearchRepository.searchAsPage(request, new CountRequest(IDX_TARGET), AttributeDictionaryEntity.class)
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

        final SearchSourceBuilder search = SearchSourceBuilder.searchSource()
                .query(QueryBuilders.prefixQuery(queryParam.getKey(), queryParam.getValue()))
                .from(0)
                .size(5);
        final var request = new SearchRequest(new String[]{IDX_TARGET}, search);
        return opensearchRepository.search(request, AttributeDictionaryEntity.class)
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
                .flatMap(entity -> opensearchRepository.save(IDX_TARGET, entity, AttributeDictionaryEntity.class))
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
                .flatMap(entity -> attributeSenderService.send(BINDING_TARGET, entity))
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
                .flatMapMany(entities -> opensearchRepository.bulk(entities, IDX_TARGET))
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
                .flatMap(entity -> attributeSenderService.send(BINDING_TARGET, entity))
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
                .flatMap(entity -> opensearchRepository.update(IDX_TARGET, id, entity, AttributeDictionaryEntity.class))
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
    public Mono<ResponseEntity<List<BulkResult>>> deleteAllAttributes() {

        final SearchSourceBuilder search = SearchSourceBuilder.searchSource()
                .from(0)
                .size(1000);

        final var request = new SearchRequest(new String[]{IDX_TARGET}, search);

        return opensearchRepository.search(request, AttributeDictionaryEntity.class)
                .doOnError(t -> log.error(t.getMessage(), t))
                .map(AttributeDictionaryEntity::getId)
                .collectList()
                .flatMap(ids -> opensearchRepository.deleteAll(IDX_TARGET, ids))
                .map(mapper::toBulkResultModels)
                .map(ResponseEntity::ok)
                .onErrorResume(t -> Mono.just(ResponseEntity.noContent().build()));
    }
}
