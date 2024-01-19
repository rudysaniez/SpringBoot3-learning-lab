package com.springboot.learning.sb3.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.MonoSink;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class ReactiveOpensearchEngineHelper {

    private ReactiveOpensearchEngineHelper() {}

    private static final Logger log = LoggerFactory.getLogger(ReactiveOpensearchEngineHelper.class);

    /**
     * @param searchResponse : the search response
     * @param type : the entity Class type
     * @param <T> : the entity type
     */
    public static <T> void fillsFlow(@NotNull ObjectMapper jack,
                                     @NotNull SearchResponse searchResponse,
                                     @NotNull Class<T> type,
                                     @NotNull MonoSink<T> sink) {

        Stream.of(searchResponse.getHits().getHits())
                .limit(1)
                .map(SearchHit::getSourceAsString)
                .map(json -> getFromJson(jack, json, type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(t -> {

                    sink.success(t);
                    return t;
                })
                .forEach(t -> {
                    log.info(" > Data is read t={}", t);
                    sink.success(t);
                });
    }

    /**
     * @param jack
     * @param searchResponse
     * @param type
     * @param sink
     * @param <T> : the entity type
     */
    public static <T> void fillsFlow(@NotNull ObjectMapper jack,
                                     @NotNull SearchResponse searchResponse,
                                     @NotNull Class<T> type,
                                     @NotNull FluxSink<T> sink) {

        Stream.of(searchResponse.getHits().getHits())
                .map(SearchHit::getSourceAsString)
                .map(json -> getFromJson(jack, json, type))
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
     *
     * @param searchResponse
     * @param type
     * @param sink
     * @param <T>
     */
    public static <T> void fillsFlowWithId(@NotNull ObjectMapper jack,
                                           @NotNull SearchResponse searchResponse,
                                           @NotNull Class<T> type,
                                           @NotNull FluxSink<T> sink) {

        Stream.of(searchResponse.getHits().getHits())
                .map(hit -> new Glue<>(hit.getId(), hit.getSourceAsString()))
                .map(glue -> new Glue<>(glue.t1(), getMapFromJson(jack, glue.t2())))
                .map(glue -> mergeIdInMap(glue.t1(), glue.t2()))
                .map(data -> mapToObject(jack, data, type))
                .forEach(t -> {
                    sink.next(t);
                    log.info(" > Data is read t={}", t);
                });
        sink.complete();
    }

    public static <T> void fillsFlowWithId(@NotNull ObjectMapper jack,
                                           @NotNull GetResponse response,
                                           @NotNull Class<T> type,
                                           @NotNull MonoSink<T> sink) {

        Stream.of(response.getSourceAsMap())
                .map(data -> mapToObject(jack, data, type))
                .forEach(t -> {
                    log.info(" > Data is read t={}", t);
                    sink.success(t);});
    }

    public record Glue<T1,T2>(T1 t1, T2 t2) {}

    /**
     * @param jack : Jack !
     * @param json : the input
     * @param type : the object type
     * @return {@link Optional of T}
     * @param <T> : the type
     */
    public static <T> Optional<T> getFromJson(@NotNull ObjectMapper jack, @NotNull String json, Class<T> type) {

        try {
            return Optional.ofNullable(jack.readValue(json, type));
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return Optional.empty();
    }

    /**
     * @param jack : Jack !
     * @param sourceJson : the entity
     * @return {@link Map}
     */
    public static Map<String, Object> getMapFromJson(@NotNull ObjectMapper jack, String sourceJson) {

        try {
            final Map<String, Object> data = jack.readValue(sourceJson, new TypeReference<>() {});
            log.info(" > This json {} has been transformed in this Map {}.", sourceJson, data);
            return data;
        }
        catch(Exception e) {
            log.error(e.getMessage(), e);
        }

        return new HashMap<>();
    }

    /**
     * @param id
     * @param data
     * @return {@link Map}
     */
    public static Map<String, Object> mergeIdInMap(String id, Map<String, Object> data) {
        data.putIfAbsent("id", id);
        return data;
    }

    /**
     * @param jack : Jack !
     * @param data : the data from {@link Map}
     * @param type
     * @return {@link T}
     * @param <T> : the parameter type
     */
    public static <T> T mapToObject(@NotNull ObjectMapper jack,
                                    @NotNull Map<String, Object> data,
                                    @NotNull Class<T> type) {
        return jack.convertValue(data, type);
    }
}
