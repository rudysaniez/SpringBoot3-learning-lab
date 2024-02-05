package com.springboot.learning.sb3.service.v1;

import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
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

@ConditionalOnProperty(prefix = "service", name = "version", havingValue = "v1")
@Service
public class AttributeDictionaryService implements IAttributeDictionaryService {

    private final ReactiveOpensearchRepository repository;
    private final String indexTarget = "attributes_dictionary_v1";

    public AttributeDictionaryService(ReactiveOpensearchRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getIndexTarget() {
        return indexTarget;
    }

    @Override
    public Mono<AttributeDictionaryEntity> getAttributeById(String id) {
        return repository.getById(indexTarget, id, AttributeDictionaryEntity.class);
    }

    @Override
    public Mono<ReactiveOpensearchRepository.Page<AttributeDictionaryEntity>> searchAsPage(int page, int size) {

        final var search = SearchSourceBuilder.searchSource()
                .from(page)
                .size(size);
        final var request = new SearchRequest(new String[]{indexTarget}, search);

        return repository.searchAsPage(request, new CountRequest(indexTarget), AttributeDictionaryEntity.class);
    }

    @Override
    public Flux<AttributeDictionaryEntity> searchWithQueryPrefix(String field, String value, int limit) {

        final SearchSourceBuilder search = SearchSourceBuilder.searchSource()
                .query(QueryBuilders.prefixQuery(field, value))
                .from(0)
                .size(limit);
        final var request = new SearchRequest(new String[]{indexTarget}, search);

        return repository.search(request, AttributeDictionaryEntity.class);
    }

    @Override
    public Flux<AttributeDictionaryEntity> searchNoPredicateButLimited(int limit) {

        final SearchSourceBuilder search = SearchSourceBuilder.searchSource()
                .from(0)
                .size(limit);
        final var request = new SearchRequest(new String[]{indexTarget}, search);
        return repository.search(request, AttributeDictionaryEntity.class);
    }

    @Override
    public Mono<AttributeDictionaryEntity> save(AttributeDictionaryEntity entity) {
        return repository.save(indexTarget, entity, AttributeDictionaryEntity.class);
    }

    @Override
    public Flux<ReactiveOpensearchRepository.CrudResult> bulk(List<AttributeDictionaryEntity> entities) {
        return repository.bulk(entities, indexTarget);
    }

    @Override
    public Mono<AttributeDictionaryEntity> update(String id, AttributeDictionaryEntity entity) {
        return repository.update(indexTarget, id, entity, AttributeDictionaryEntity.class);
    }

    @Override
    public Mono<Integer> deleteOne(String id) {
        return repository.delete(indexTarget, id);
    }

    @Override
    public Mono<List<ReactiveOpensearchRepository.CrudResult>> deleteAll(List<String> ids) {
        return repository.deleteAll(indexTarget, ids);
    }
}