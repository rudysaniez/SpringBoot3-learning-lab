package com.springboot.learning.sb3.service;

import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.dto.Video;
import com.springboot.learning.sb3.dto.VideoDeletion;
import com.springboot.learning.sb3.dto.VideoSearch;
import com.springboot.learning.sb3.mapper.VideoMapper;
import com.springboot.learning.sb3.repository.VideoRepository;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class VideoService {

    private final VideoRepository videoRepository;

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    /**
     * @param page : the page number
     * @param size : the size
     * @return page of {@link VideoEntity}
     */
    public Flux<VideoEntity> findAll(int page, int size) {
        return videoRepository.findAll();
    }

    /**
     * @param name : the video name
     * @return {@link List<VideoEntity>}
     */
    public Flux<VideoEntity> findByName(String name) {
        return videoRepository.findByName(name);
    }

    /**
     * @return number of {@link VideoEntity}
     */
    public Mono<Long> count() {
        return videoRepository.count();
    }

    /**
     * @param video : a video
     * @param username : the user
     * @return {@link VideoEntity}
     */
    public Mono<VideoEntity> save(@NotNull Video video,
                            @NotNull String username) {

        final VideoEntity entity = new VideoEntity(null, video.name(), video.description(), username);
        return videoRepository.save(entity);
    }

    /**
     * @param videoSearch : a video search criteria
     * @return list of {@link VideoEntity}
     */
    public Flux<VideoEntity> search(VideoSearch videoSearch) {

        log.info(" > Search many videos by {}.", videoSearch);

        if(StringUtils.hasText(videoSearch.name()))
            return videoRepository.findTop3ByNameContainsIgnoreCaseOrderByName(videoSearch.name());
        else if (videoSearch.description().isPresent() && StringUtils.hasText(videoSearch.description().get()))
            return videoRepository.findTop3ByDescriptionContainingIgnoreCaseOrderByName(videoSearch.description().get());

        return Flux.empty();
    }

    /**
     * Delete by {@link VideoDeletion}.
     * @param videoDeletion : the video deletion
     */
    public Flux<VideoEntity> delete(@NotNull VideoDeletion videoDeletion) {

        return this.findByName(videoDeletion.name())
                .flatMap(videoEntity -> videoRepository.delete(videoEntity)
                                        .thenReturn(videoEntity)
                );
    }
}
