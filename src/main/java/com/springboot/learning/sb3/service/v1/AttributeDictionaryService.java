package com.springboot.learning.sb3.service.v1;

import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.exception.InvalidInputException;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import com.springboot.learning.sb3.service.IAttributeDictionaryService;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.core.CountRequest;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@ConditionalOnProperty(prefix = "service", name = "version", havingValue = "v1")
@Service
public class AttributeDictionaryService implements IAttributeDictionaryService<AttributeDictionaryEntity> {

    private final ReactiveOpensearchRepository repository;
    private static final String INDEX_TARGET = "attributes_dictionary_v1";

    public AttributeDictionaryService(ReactiveOpensearchRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<AttributeDictionaryEntity> getAttributeById(String id) {
        return repository.getById(INDEX_TARGET, id, AttributeDictionaryEntity.class);
    }

    @Override
    public Mono<ReactiveOpensearchRepository.Page<AttributeDictionaryEntity>> searchAsPage(int page, int size) {

        final var search = SearchSourceBuilder.searchSource()
                .from(page)
                .size(size);
        final var request = new SearchRequest(new String[]{INDEX_TARGET}, search);

        return repository.searchAsPage(request, new CountRequest(INDEX_TARGET), AttributeDictionaryEntity.class);
    }

    @Override
    public Flux<AttributeDictionaryEntity> searchWithQueryPrefix(String field, String value, int limit) {

        final SearchSourceBuilder search = SearchSourceBuilder.searchSource()
                .query(QueryBuilders.prefixQuery(field, value))
                .from(0)
                .size(limit);
        final var request = new SearchRequest(new String[]{INDEX_TARGET}, search);

        return repository.search(request, AttributeDictionaryEntity.class);
    }

    @Override
    public Flux<AttributeDictionaryEntity> searchNoPredicateButLimited(int limit) {

        final SearchSourceBuilder search = SearchSourceBuilder.searchSource()
                .from(0)
                .size(limit);
        final var request = new SearchRequest(new String[]{INDEX_TARGET}, search);
        return repository.search(request, AttributeDictionaryEntity.class);
    }

    @Override
    public Mono<AttributeDictionaryEntity> save(AttributeDictionaryEntity entity) {

        if(Objects.isNull(entity.code()))
            throw new InvalidInputException("The code field in attribute dictionary is mandatory");

        return repository.save(INDEX_TARGET, entity, AttributeDictionaryEntity.class);
    }

    @Override
    public Flux<ReactiveOpensearchRepository.CrudResult> bulk(List<AttributeDictionaryEntity> entities) {
        return repository.bulk(entities, INDEX_TARGET);
    }

    @Override
    public Mono<AttributeDictionaryEntity> update(String id, AttributeDictionaryEntity entity) {
        return repository.update(INDEX_TARGET, id, entity, AttributeDictionaryEntity.class);
    }

    @Override
    public Mono<Integer> deleteOne(String id) {
        return repository.delete(INDEX_TARGET, id);
    }

    @Override
    public Mono<List<ReactiveOpensearchRepository.CrudResult>> deleteAll(List<String> ids) {
        return repository.deleteAll(INDEX_TARGET, ids);
    }
}
