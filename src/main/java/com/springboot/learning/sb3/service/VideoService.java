package com.springboot.learning.sb3.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.dto.Video;
import com.springboot.learning.sb3.dto.VideoDeletion;
import com.springboot.learning.sb3.dto.VideoSearch;
import com.springboot.learning.sb3.repository.VideoRepository;
import jakarta.validation.constraints.NotNull;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Response;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.core.action.ActionListener;
import org.opensearch.data.client.orhlc.ClientConfiguration;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final RestHighLevelClient highLevelClient;

    private final ObjectMapper jack;

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    public VideoService(VideoRepository videoRepository,
                        RestHighLevelClient highLevelClient,
                        ObjectMapper jack) {

        this.videoRepository = videoRepository;
        this.highLevelClient = highLevelClient;
        this.jack = jack;
    }

    /**
     * @param pageNumber : the page number
     * @param pageSize : the size
     * @return page of {@link VideoEntity}
     */
    public Flux<VideoEntity> findAll(int pageNumber, int pageSize) {
        return search(new SearchRequest("videos"), VideoEntity.class);
    }

    /**
     *
     * @return flow of {@link VideoEntity}
     */
    public Mono<VideoEntity> findFirst() {

        try {

            final SearchRequest searchRequest = new SearchRequest("videos");
            SearchResponse response = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            log.info(" > query result ={}", Stream.of(response.getHits().getHits()).toList());

            var result = Stream.of(response.getHits().getHits())
                    .map(SearchHit::getSourceAsString)
                    .map(json -> getFromJson(json, VideoEntity.class))
                    .toList();
            log.info(" > Objects are {}.", result);

            return Mono.create(sink ->

                highLevelClient.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                    @Override
                    public void onResponse(SearchResponse searchResponse) {
                        fillsFlow(searchResponse, VideoEntity.class, sink);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        sink.error(e);
                    }
                })
            );
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return Mono.empty();
    }

    /**
     *
     * @param searchRequest
     * @param type
     * @return
     * @param <T>
     */
    protected <T> Flux<T> search(SearchRequest searchRequest, Class<T> type) {

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
     * @return
     */
    Flux<VideoEntity> findAll() {

        final SearchRequest searchRequest = new SearchRequest("videos");

        return Flux.create(fluxSink ->

            highLevelClient.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    fillsFlow(searchResponse, VideoEntity.class, fluxSink);
                }

                @Override
                public void onFailure(Exception e) {
                    fluxSink.error(e);
                }
            })
        );
    }

    /**
     * @param fieldName
     * @param name
     * @return
     */
    Flux<VideoEntity> findByName(String fieldName, String name) {

        final SearchRequest searchRequest = new SearchRequest("videos");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchPhraseQuery(fieldName, name));
        searchRequest.source(sourceBuilder);

        return Flux.create(fluxSink ->

                highLevelClient.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                    @Override
                    public void onResponse(SearchResponse searchResponse) {
                        fillsFlow(searchResponse, VideoEntity.class, fluxSink);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fluxSink.error(e);
                    }
                })
        );
    }

    /**
     * @param searchResponse
     * @param type
     * @param <T> : the entity type
     */
    protected <T> void fillsFlow(@NotNull SearchResponse searchResponse, @NotNull Class<T> type, @NotNull MonoSink<T> sink) {
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
    protected <T> void fillsFlow(@NotNull SearchResponse searchResponse, @NotNull Class<T> type, @NotNull FluxSink<T> sink) {
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

    /**
     * @param name : the video name
     * @return {@link List<VideoEntity>}
     */
    public Flux<VideoEntity> findByName(String name) {

        final SearchRequest searchRequest = new SearchRequest("videos");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchPhraseQuery("videoName", name));
        searchRequest.source(sourceBuilder);
        return search(searchRequest, VideoEntity.class);
    }

    /**
     * @param name : the name
     * @param username : the username
     * @return flow of {@link VideoEntity}
     */
    public Mono<VideoEntity> findByNameAndUsername(String name, String username) {
        //return Mono.just(videoRepository.findByNameAndUsername(name, username));
        return Mono.empty();
    }

    /**
     * @return number of {@link VideoEntity}
     */
    public Mono<Long> count() {
        //return videoRepository.count();
        return Mono.just(0L);
    }

    /**
     * @param video : a video
     * @param username : the user
     * @return {@link VideoEntity}
     */
    public Mono<VideoEntity> save(@NotNull Video video,
                                  @NotNull String username) {

        log.info(" > Create a video with {} by username {}.", video,username);

        VideoEntity v = videoRepository.save(new VideoEntity(null, video.videoName(), video.description(), username));

        return Mono.just(v);
    }

    /**
     * @param videoSearch : a video search criteria
     * @return list of {@link VideoEntity}
     */
    public Flux<VideoEntity> search(VideoSearch videoSearch) {

        log.info(" > Search many videos by {}.", videoSearch);

        /*
        if(StringUtils.hasText(videoSearch.name()))
            return videoRepository.findTop3ByNameContainsIgnoreCaseOrderByName(videoSearch.name());
        else if (videoSearch.description().isPresent() && StringUtils.hasText(videoSearch.description().get()))
            return videoRepository.findTop3ByDescriptionContainingIgnoreCaseOrderByName(videoSearch.description().get());
         */
        return Flux.empty();
    }

    /**
     * Delete by {@link VideoDeletion}.
     * @param videoDeletion : the video deletion
     */
    public Mono<VideoEntity> delete(@NotNull VideoDeletion videoDeletion, Authentication authentication) {

        log.info(" > Delete video by name={} and by username={}.",
                videoDeletion,
                authentication.getName());

        /*
        return this.findByNameAndUsername(videoDeletion.name(), authentication.getName())
                .flatMap(videoEntity -> videoRepository.delete(videoEntity)
                                            .thenReturn(videoEntity));
         */
        return Mono.empty();
    }
}
