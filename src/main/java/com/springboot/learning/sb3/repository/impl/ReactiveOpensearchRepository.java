package com.springboot.learning.sb3.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.service.VideoService;
import jakarta.validation.constraints.NotNull;
import org.opensearch.action.DocWriteRequest;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkRequestBuilder;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.CountRequest;
import org.opensearch.client.core.CountResponse;
import org.opensearch.core.action.ActionListener;
import org.opensearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public class ReactiveOpensearchRepository {

    private final RestHighLevelClient highLevelClient;
    private final ObjectMapper jack;

    private static final Logger log = LoggerFactory.getLogger(ReactiveOpensearchRepository.class);

    public ReactiveOpensearchRepository(RestHighLevelClient highLevelClient, ObjectMapper jack) {
        this.highLevelClient = highLevelClient;
        this.jack = jack;
    }

    /**
     *
     * @param searchRequest : the search request
     * @param type : the entity Class type
     * @return {@link Flux}
     * @param <T> : the entity type
     */
    public <T> Flux<T> search(@NotNull SearchRequest searchRequest, @NotNull Class<T> type) {

        return Flux.create(fluxSink ->
            highLevelClient.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    fillsFlow(searchResponse, type, fluxSink);
                }

                @Override
                public void onFailure(Exception e) {
                    fluxSink.error(e);
                }
            })
        );
    }

    /**
     * @param countRequest : the count request
     * @return flow of {@link Long}
     */
    public Mono<Long> count(@NotNull CountRequest countRequest) {

        return Mono.create(sink ->
            highLevelClient.countAsync(countRequest, RequestOptions.DEFAULT, new ActionListener<CountResponse>() {
                @Override
                public void onResponse(CountResponse countResponse) {
                    sink.success(countResponse.getCount());
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            })
        );
    }

    /**
     * @param indexName : the index name
     * @param entity : the entity
     * @return {@link Mono of rest status}
     * @param <T> : the parameter type
     */
    public <T> Mono<Integer> insert(@NotNull String indexName, @NotNull T entity) {

        return Mono.create(sink -> {
            final Map<String, Object> data = jack.convertValue(entity, new TypeReference<>() {});
            final IndexRequest indexRequest = new IndexRequest(indexName).source(data);

            highLevelClient.indexAsync(indexRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(IndexResponse indexResponse) {
                    sink.success(indexResponse.status().getStatus());
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            });
        });
    }

    /**
     *
     * @param indexName
     * @param entity
     * @param type
     * @return
     * @param <T>
     */
    public <T> Mono<T> save(@NotNull String indexName, @NotNull T entity, Class<T> type) {

        return Mono.<String>create(sink -> {
            final Map<String, Object> data = jack.convertValue(entity, new TypeReference<>() {});
            final IndexRequest indexRequest = new IndexRequest(indexName).source(data);

            highLevelClient.indexAsync(indexRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(IndexResponse indexResponse) {
                    sink.success(indexResponse.getId());
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            });
        })
        .map(id -> new GetRequest(indexName, id))
        .doOnNext(id -> log.info(" > The id of entity created is {}.", id.id()))
        .flatMap(getRequest -> Mono.<T>create(sink ->
            highLevelClient.getAsync(getRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(GetResponse documentFields) {

                    log.info(" > The entity created has been find and the content is {}", documentFields.getSourceAsString());
                    final Optional<T> entity = getFromJson(documentFields.getSourceAsString(), type);
                    if(entity.isPresent())
                        sink.success(entity.get());
                    else
                        sink.success();
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            }))
        )
        .doOnNext(entityAfterInsert -> log.info(" > The entity after persistence is {}", entityAfterInsert));
    }

    /**
     * @param entities : the list of entity
     * @param indexName : the index name
     * @return {@link Flux of rest status}
     * @param <T> : the parameter type
     */
    public <T> Flux<Integer> bulk(@NotNull List<T> entities, @NotNull String indexName) {

       var bulk = new BulkRequest();

        var indexRequest = new IndexRequest(indexName);
        entities.stream()
            .map(e -> jack.convertValue(e, new TypeReference<Map<String, Object>>() {}))
            .forEach(indexRequest::source);

        bulk.add(indexRequest);

        return Flux.create(sink ->
            highLevelClient.bulkAsync(bulk, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    sink.next(bulkItemResponses.status().getStatus());
                    sink.complete();
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            })
        );
    }

    /**
     * @param searchResponse : the search response
     * @param type : the entity Class type
     * @param <T> : the entity type
     */
    protected <T> void fillsFlow(@NotNull SearchResponse searchResponse,
                                 @NotNull Class<T> type,
                                 @NotNull MonoSink<T> sink) {

        Stream.of(searchResponse.getHits().getHits())
                .limit(1)
                .map(SearchHit::getSourceAsString)
                .map(json -> getFromJson(json, type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(t -> {

                    sink.success(t);
                    return t;
                })
                .forEach(t -> log.info(" > Data is read t={}", t));
    }

    /**
     * @param searchResponse
     * @param type
     * @param sink
     * @param <T> : the entity type
     */
    protected <T> void fillsFlow(@NotNull SearchResponse searchResponse,
                                 @NotNull Class<T> type,
                                 @NotNull FluxSink<T> sink) {

        Stream.of(searchResponse.getHits().getHits())
                .map(SearchHit::getSourceAsString)
                .map(json -> getFromJson(json, type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(t -> {
                    sink.next(t);
                    return t;
                })
                .forEach(t -> log.info(" > Data is read t={}", t));
        sink.complete();
    }

    /**
     * @param json : the input
     * @param type : the object type
     * @return {@link Optional of T}
     * @param <T> : the type
     */
    protected <T> Optional<T> getFromJson(@NotNull String json, Class<T> type) {

        try {
            return Optional.ofNullable(jack.readValue(json, type));
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return Optional.empty();
    }
}