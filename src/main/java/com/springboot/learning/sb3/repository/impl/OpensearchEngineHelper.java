package com.springboot.learning.sb3.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.search.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.MonoSink;

import java.util.Map;
import java.util.stream.Stream;

public abstract class OpensearchEngineHelper {

    private OpensearchEngineHelper() {}

    private static final Logger log = LoggerFactory.getLogger(OpensearchEngineHelper.class);

    /**
     * @param jack : Jack !
     * @param searchResponse : the get response
     * @param type : The entity type
     * @param sink : The sink
     * @param <T> : the parameter type
     */
    public static <T> void fillsFlowWithId(@NotNull ObjectMapper jack,
                                           @NotNull SearchResponse searchResponse,
                                           @NotNull Class<T> type,
                                           @NotNull FluxSink<T> sink) {

        Stream.of(searchResponse.getHits().getHits())
                .map(hit -> OpensearchEngineHelper.mergeIdInMap(hit.getId(), hit.getSourceAsMap()))
                .map(data -> OpensearchEngineHelper.mapToObject(jack, data, type))
                .forEach(t -> {
                    sink.next(t);
                    log.debug(" > Data is read t={}", t);
                });
        sink.complete();
    }

    /**
     * @param jack : Jack !
     * @param response : the get response
     * @param type : The entity type
     * @param sink : The sink
     * @param <T> : the parameter type
     */
    public static <T> void fillsFlowWithId(@NotNull ObjectMapper jack,
                                           @NotNull GetResponse response,
                                           @NotNull Class<T> type,
                                           @NotNull MonoSink<T> sink) {

        Stream.of(response.getSourceAsMap())
                .map(data -> OpensearchEngineHelper.mergeIdInMap(response.getId(), data))
                .map(data -> mapToObject(jack, data, type))
                .forEach(t -> {
                    log.debug(" > Data is read t={}", t);
                    sink.success(t);});
    }

    /**
     * @param id : the identifier
     * @param data : the data in a {@link Map}
     * @return {@link Map}
     */
    public static Map<String, Object> mergeIdInMap(@NotNull String id, @NotNull Map<String, Object> data) {
        data.putIfAbsent("id", id);
        return data;
    }

    /**
     * @param jack : Jack !
     * @param data : the data from {@link Map}
     * @param type : the entity class type
     * @return {@link T}
     * @param <T> : the parameter type
     */
    public static <T> T mapToObject(@NotNull ObjectMapper jack,
                                    @NotNull Map<String, Object> data,
                                    @NotNull Class<T> type) {
        return jack.convertValue(data, type);
    }
}
