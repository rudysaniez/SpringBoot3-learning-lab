package com.springboot.learning.sb3.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.dto.Video;
import com.springboot.learning.sb3.dto.VideoDeletion;
import com.springboot.learning.sb3.repository.VideoRepository;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import jakarta.validation.constraints.NotNull;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.CountRequest;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final RestHighLevelClient highLevelClient;
    private final ObjectMapper jack;
    private final ReactiveOpensearchRepository opensearchRepository;

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    public VideoService(VideoRepository videoRepository,
                        RestHighLevelClient highLevelClient,
                        ObjectMapper jack,
                        ReactiveOpensearchRepository opensearchRepository) {

        this.videoRepository = videoRepository;
        this.highLevelClient = highLevelClient;
        this.jack = jack;
        this.opensearchRepository = opensearchRepository;
    }

    /**
     * @return page of {@link VideoEntity}
     */
    public Flux<VideoEntity> findAll() {
        return opensearchRepository.search(new SearchRequest("videos"), VideoEntity.class);
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
        return opensearchRepository.search(searchRequest, VideoEntity.class);
    }

    /**
     * @return number of {@link VideoEntity}
     */
    public Mono<Long> count() {
        return opensearchRepository.count(new CountRequest("videos"));
    }

    /**
     * @param chunkOfName : the chunk of name
     * @return flow of {@link VideoEntity}
     */
    public Flux<VideoEntity> searchByNameContaining(String chunkOfName) {

        log.info(" > Search videos by name containing that {}.", chunkOfName);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchPhrasePrefixQuery("videoName", chunkOfName));
        final SearchRequest searchRequest = new SearchRequest("videos");
        searchRequest.source(builder);
        return opensearchRepository.search(searchRequest, VideoEntity.class);
    }

    /**
     * @param video : a video
     * @param username : the user
     * @return {@link VideoEntity}
     */
    public Mono<VideoEntity> save(@NotNull Video video, @NotNull String username) {

        log.info(" > Create a video with {} by username {}.", video,username);

        var videoEntity = new VideoEntity(null, video.videoName(), video.description(), username);

        return opensearchRepository.save("videos", videoEntity, VideoEntity.class);

//        return opensearchRepository.insert("videos", videoEntity)
//                .thenReturn(videoEntity);
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
