package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.dto.*;
import com.springboot.learning.sb3.mapper.VideoMapper;
import com.springboot.learning.sb3.service.VideoService;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RestController
public class VideoRestController {

    private final VideoService videoService;
    private static final VideoMapper videoMapper = Mappers.getMapper(VideoMapper.class);

    private static final Logger log = LoggerFactory.getLogger(VideoRestController.class);

    public VideoRestController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * @param name : the name
     * @return Mono of {@link ResponseEntity<Video>}
     */
    @GetMapping(value = "/videos/{videoName}")
    public Mono<ResponseEntity<Video>> getById(@PathVariable(value = "videoName") String name) {
        return videoService.findByName(name)
                .next()
                .map(videoMapper::toModel)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get me all !
     * @return list of {@link Video}
     */
    @GetMapping(value = "/videos", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Video> all(@RequestParam(value = "page", required = false) Integer page,
                           @RequestParam(value = "size", required = false) Integer size,
                           Authentication authentication) {

        if(Objects.isNull(page)) page = 0;
        if(Objects.isNull(size)) size = 20;
        if(page < 0) page = 0;
        if(size > 20) size = 20;

        log.info(" > All videos by the user {}.", authentication.getName());

        return videoService.findAll(page, size)
                .map(videoMapper::toModel);
    }

    /**
     * Count {@link Video}
     * @return number of {@link Video}
     */
    @GetMapping(value = "/videos/:count", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Long>> count() {

        return videoService.count()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Search system.
     * @param name : a video search criteria
     * @return list of {@link Video}
     */
    @GetMapping(value = "/videos/:search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Video> search(@RequestParam(name = "name") String name) {

        return videoService.search(new VideoSearch(name, Optional.empty()))
                .map(videoMapper::toModel);
    }

    /**
     * Creation of videos.
     * @param video : a video in the request body
     * @return {@link Video}
     */
    @PostMapping(value = "/videos",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Video>> create(@RequestBody Video video,
                                              Authentication authentication) {

        return Mono.just(video)
                .flatMap(v -> videoService.save(v, authentication.getName()))
                .map(videoMapper::toModel)
                .map(ResponseEntity::ok);
    }

    /**
     * Delete videos.
     * @param name : The name of videos to deleted
     * @return {@link List<Video>}
     */
    @DeleteMapping(value = "/videos", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> deleteByName(@RequestParam(name = "name") String name) {

        if(!StringUtils.hasText(name))
            throw new IllegalArgumentException(" > The query parameter name is mandatory");

        return videoService.delete(new VideoDeletion(name))
                .collectList()
                .filter(videoEntities -> !videoEntities.isEmpty())
                .map(data -> ResponseEntity.ok().<Void>build())
                .switchIfEmpty(Mono.just(ResponseEntity.noContent().build()));
    }
}
