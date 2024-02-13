package com.springboot.learning.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.action.update.UpdateResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.CountRequest;
import org.opensearch.client.core.CountResponse;
import org.opensearch.core.action.ActionListener;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.reindex.BulkByScrollResponse;
import org.opensearch.index.reindex.DeleteByQueryRequest;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public class ReactiveOpensearchRepository {

    private final RestHighLevelClient highLevelClient;
    private final ObjectMapper jack;

    private static final String NO_SUCH_INDEX = "no such index";
    private static final Logger log = LoggerFactory.getLogger(ReactiveOpensearchRepository.class);

    public ReactiveOpensearchRepository(RestHighLevelClient highLevelClient, ObjectMapper jack) {
        this.highLevelClient = highLevelClient;
        this.jack = jack;
    }

    /**
     * @param indexName : the index name
     * @param id : the identifier
     * @param type : the entity type
     * @return {@link Mono}
     * @param <T> : the parameter type
     */
    public <T> Mono<T> getById(@NotNull String indexName,
                               @NotNull String id,
                               @NotNull Class<T> type) {

        return Mono.<T>create(sink -> {
            final var request = new GetRequest(indexName, id);
            highLevelClient.getAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(GetResponse getResponse) {

                    if(getResponse.isExists())
                        OpensearchEngineHelper.fillsFlowWithId(jack, getResponse, type, sink);
                    else
                        sink.success();
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            });
        })
        .doOnError(t -> log.error(t.getMessage(), t))
        .onErrorResume(t -> t.getMessage().contains(NO_SUCH_INDEX), throwable -> Mono.empty());
    }

    /**
     * @param searchRequest : the search request
     * @param type : the entity Class type
     * @return {@link Flux}
     * @param <T> : the parameter type
     */
    public <T> Flux<T> search(@NotNull SearchRequest searchRequest, @NotNull Class<T> type) {

        return Flux.<T>create(fluxSink ->
            highLevelClient.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    OpensearchEngineHelper.fillsFlowWithId(jack, searchResponse, type, fluxSink);
                }

                @Override
                public void onFailure(Exception e) {
                    fluxSink.error(e);
                }
            })
        )
        .doOnError(t -> log.error(t.getMessage(), t))
        .onErrorResume(t -> t.getMessage().contains(NO_SUCH_INDEX), throwable -> Mono.empty());
    }

    /**
     * @param searchRequest : the search request
     * @param countRequest : the count request
     * @param type : the entity type
     * @return flow of {@link Page}
     * @param <T> : the parameter type
     */
    public <T> Mono<Page<T>> searchAsPage(@NotNull SearchRequest searchRequest,
                                          @NotNull CountRequest countRequest,
                                          @NotNull Class<T> type) {

        final SearchSourceBuilder query = searchRequest.source();

        return this.search(searchRequest, type)
                .collectList()
                .zipWith(this.count(countRequest))
                .map(tuple2 -> new Page<>(tuple2.getT1(), new PageMetadata(query.from(), query.size(), tuple2.getT2(),
                                                    tuple2.getT2() > query.size() ? Math.ceilDiv(tuple2.getT2(), query.size()) : 1)));
    }

    public record Page<T>(List<T> content, PageMetadata pageMetadata) {}
    public record PageMetadata(int number, int size, long totalElements, long totalPages) {}

    /**
     * @param countRequest : the count request
     * @return flow of {@link Long}
     */
    public Mono<Long> count(@NotNull CountRequest countRequest) {

        return Mono.<Long>create(sink ->
            highLevelClient.countAsync(countRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(CountResponse countResponse) {
                    sink.success(countResponse.getCount());
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            })
        )
        .doOnError(t -> log.error(t.getMessage(), t))
        .onErrorResume(t -> t.getMessage().contains(NO_SUCH_INDEX), throwable -> Mono.empty());
    }

    /**
     * @param indexName : the index name
     * @param entity : the entity that need to created
     * @param type : the entity class type
     * @return flow of {@link T}
     * @param <T> : the parameter type
     */
    public <T> Mono<T> save(@NotNull String indexName,
                            @NotNull T entity,
                            @NotNull Class<T> type) {

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
        .doOnNext(id -> log.debug(" > The id of entity created is {}.", id.id()))
        .flatMap(getRequestById -> Mono.<T>create(sink ->
            highLevelClient.getAsync(getRequestById, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(GetResponse documentFields) {
                    log.debug(" > The entity created has been find, the id is {}, and the content is {}.",
                            documentFields.getId(),
                            documentFields.getSourceAsMap());
                    final Map<String, Object> data = documentFields.getSourceAsMap();
                    OpensearchEngineHelper.mergeIdInMap(documentFields.getId(), data);
                    sink.success(OpensearchEngineHelper.mapToObject(jack, data, type));
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            }))
        )
        .doOnNext(entityAfterInsert -> log.debug(" > The entity after persistence is {}", entityAfterInsert));
    }

    /**
     * @param entities : the list of entity
     * @param indexName : the index name
     * @return {@link Flux of crud result that contain the identifier and the status code}
     * @param <T> : the parameter type
     */
    public <T> Flux<CrudResult> bulk(@NotNull List<T> entities, @NotNull String indexName) {

        final var bulk = new BulkRequest();

        entities.stream()
            .map(e -> jack.convertValue(e, new TypeReference<Map<String, Object>>() {}))
            .map(dataAsMap -> new IndexRequest(indexName).source(dataAsMap))
            .forEach(bulk::add);

        return Mono.<List<CrudResult>>create(sink ->
            highLevelClient.bulkAsync(bulk, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    final List<CrudResult> crudResult = Arrays.stream(bulkItemResponses.getItems())
                                    .map(bulkItem -> new CrudResult(bulkItem.getId(), bulkItem.status().getStatus()))
                                    .toList();
                    sink.success(crudResult);
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            })
        )
        .flatMapIterable(results -> results);
    }

    /**
     *
     * @param indexName : the index name
     * @param id : the identifier
     * @param entity : the entity
     * @param type : the entity type
     * @return {@link T}
     * @param <T> : the parameterized type
     */
    public <T> Mono<T> update(@NotNull String indexName,
                              @NotNull String id,
                              @NotNull T entity,
                              @NotNull Class<T> type) {

        final var update = new UpdateRequest(indexName, id);
        update.doc(jack.convertValue(entity, new TypeReference<Map<String, Object>>() {}));

        return Mono.create(sink ->
            highLevelClient.updateAsync(update, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(UpdateResponse updateResponse) {
                    sink.success(updateResponse.status().getStatus());
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            })
        )
        .filter(status -> status.equals(200))
        .flatMap(useless -> getById(indexName, id, type));
    }

    /**
     * @param indexName : the index name
     * @param id : the identifier
     * @return flow of {@link Integer that is the status}
     */
    public Mono<Integer> delete(@NotNull String indexName, @NotNull String id) {

        return Mono.<Integer>create(sink -> {
            final var request = new DeleteRequest(indexName, id);
            highLevelClient.deleteAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(DeleteResponse deleteResponse) {
                    sink.success(deleteResponse.status().getStatus());
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            });
        })
        .doOnError(t -> log.error(t.getMessage(), t))
        .onErrorResume(t -> t.getMessage().contains(NO_SUCH_INDEX), throwable -> Mono.empty());
    }

    /**
     *
     * @param indexName : the index name
     * @param ids : the list of identifiers
     * @return flow of {@link List<CrudResult>}
     */
    public Mono<List<CrudResult>> deleteIn(@NotNull String indexName,
                                           @NotNull List<String> ids) {

        BulkRequest bulkRequest = new BulkRequest();
        ids.forEach(id ->bulkRequest.add(new DeleteRequest(indexName, id)));

        return Mono.<List<CrudResult>>create(sink ->
            highLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    final List<CrudResult> crudResult = Arrays.stream(bulkItemResponses.getItems())
                            .map(bulkItem -> new CrudResult(bulkItem.getId(), bulkItem.status().getStatus()))
                            .toList();
                    sink.success(crudResult);
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            })
        )
        .doOnError(t -> log.error(t.getMessage(), t))
        .onErrorResume(t -> t.getMessage().contains(NO_SUCH_INDEX), throwable -> Mono.empty());
    }

    /**
     * @param indexName : the index name
     * @return {@link long}
     */
    public Mono<Long> deleteAll(@NotNull String indexName) {

        DeleteByQueryRequest request =  new DeleteByQueryRequest(indexName);
        request.setQuery(QueryBuilders.matchAllQuery());

        return Mono.<Long>create(sink ->
            highLevelClient.deleteByQueryAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                    sink.success(bulkByScrollResponse.getStatus().getDeleted());
                }

                @Override
                public void onFailure(Exception e) {
                    sink.error(e);
                }
            })
        )
        .doOnError(t -> log.error(t.getMessage(), t))
        .onErrorResume(t -> t.getMessage().contains(NO_SUCH_INDEX), throwable -> Mono.empty());
    }

    public record CrudResult(String id, int status) {}
}
