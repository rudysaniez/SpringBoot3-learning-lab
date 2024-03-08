package com.springboot.learning.repository.impl;

import jakarta.validation.constraints.NotNull;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.CreateIndexResponse;
import org.opensearch.client.indices.GetMappingsRequest;
import org.opensearch.client.indices.GetMappingsResponse;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.core.action.ActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReactiveOpensearchMappingRepository {

    private final RestHighLevelClient highLevelClient;

    private static final String NO_SUCH_INDEX = "no such index";

    private static final Logger log = LoggerFactory.getLogger(ReactiveOpensearchMappingRepository.class);

    public ReactiveOpensearchMappingRepository(RestHighLevelClient highLevelClient) {
        this.highLevelClient = highLevelClient;
    }

    /**
     * @param indexName : the index name
     * @return flow of {@link Map}
     */
    public Mono<Map<String, Object>> getMapping(@NotNull String indexName) {

        GetMappingsRequest mappingsRequest = new GetMappingsRequest();
        mappingsRequest.indices(indexName);

        return Mono.<Map<String, Object>>create(sink ->
            highLevelClient.indices()
                .getMappingAsync(mappingsRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                    @Override
                    public void onResponse(GetMappingsResponse getMappingsResponse) {
                        sink.success(getMappingsResponse.mappings().get(indexName).getSourceAsMap());
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
     * @param mapping : the mapping
     * @return flow of {@link Boolean}
     */
    public Mono<Boolean> createIndex(@NotNull String indexName,
                                     @NotNull Resource mapping,
                                     boolean displayErrorIfResourceAlreadyExist) {

        Optional<String> indexMapping = Optional.empty();
        try {
            indexMapping = Optional.of(mapping.getContentAsString(StandardCharsets.UTF_8));
        }
        catch(IOException e) {
            log.error(e.getMessage(), e);
        }

        if(indexMapping.isPresent()) {

            final String indexMappingAsString = indexMapping.get();

            final CreateIndexRequest mappingsRequest = new CreateIndexRequest(indexName);
            mappingsRequest.source(indexMappingAsString, XContentType.JSON);

            return Mono.<Boolean>create(sink ->
                highLevelClient.indices()
                    .createAsync(mappingsRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                        @Override
                        public void onResponse(CreateIndexResponse acknowledgedResponse) {
                            sink.success(acknowledgedResponse.isAcknowledged());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            sink.error(e);
                        }
                    })
            )
            .doOnError(t -> {
                if(displayErrorIfResourceAlreadyExist)
                    log.error(t.getMessage(), t);
            })
            .onErrorResume(t -> t.getMessage().contains("resource_already_exists_exception"),
                throwable -> Mono.just(false))
            .doOnNext(result -> {
               if(result)
                   log.info(" > This index {} has been created, the mapping is {}.", indexName, indexMappingAsString);
               else
                   log.info(" > This index {} already exists.", indexName);
            });
        }

        return Mono.just(false);
    }
}
