package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.dto.*;
import com.springboot.learning.sb3.mapper.VideoMapper;
import com.springboot.learning.sb3.service.VideoService;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    public VideoRestController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * Get me all !
     * @return list of {@link Video}
     */
    @GetMapping(value = "/videos", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Video> all(@RequestParam(value = "page", required = false) Integer page,
                                              @RequestParam(value = "size", required = false) Integer size) {

        if(Objects.isNull(page)) page = 0;
        if(Objects.isNull(size)) size = 20;
        if(page < 0) page = 0;
        if(size > 20) size = 20;

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
    public Mono<ResponseEntity<Video>> create(@RequestBody Video video) {

        return Mono.just(video)
                .flatMap(v -> videoService.save(v, "authentication.getName()"))
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

        return videoService.delete(new VideoDeletion(name))
                .map(result -> {

                    if(result)
                        return ResponseEntity.ok().build();
                    else
                        return ResponseEntity.noContent().build();
                });
    }
}
