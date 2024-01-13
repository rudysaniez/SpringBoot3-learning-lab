package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.dto.*;
import com.springboot.learning.sb3.mapper.VideoMapper;
import com.springboot.learning.sb3.service.VideoService;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
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
import java.util.function.Function;
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
     *
     * @param page : page number
     * @param size : page size
     * @return flux of {@link Video}
     */
    @GetMapping(value = "/videos", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Video> all(@RequestParam(value = "page", required = false) Integer page,
                           @RequestParam(value = "size", required = false) Integer size) {

        final int p;
        final int s;

        if(Objects.isNull(page))
            p = 0;
        else
            p = page;

        if(Objects.isNull(size))
            s = 20;
        else
            s = size;

        return videoService.findAll(p, s)
                .map(videoMapper::toModel);
    }

    /**
     * Get me all !
     * @return list of {@link Video}
     */
    @GetMapping(value = "/videos/:asPage")
    public Mono<ResponseEntity<PagedModel<EntityModel<Video>>>> allAsPage(@RequestParam(value = "page", required = false) Integer page,
                                                    @RequestParam(value = "size", required = false) Integer size,
                                                    Authentication authentication) {

        final int p;
        final int s;

        if(Objects.isNull(page))
            p = 0;
        else
            p = page;

        if(Objects.isNull(size))
            s = 20;
        else
            s = size;

        log.info(" > All videos with page by the user {}.", authentication.getName());

        final Mono<Link> linkSelf = WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(VideoRestController.class)
                                    .allAsPage(p, s, authentication))
                                    .withSelfRel()
                                    .toMono();

        return videoService.findAll(page, size)
                .map(videoMapper::toModel)
                .flatMap(video -> buildVideoLinks(video, authentication)
                        .collectList()
                        .map(links -> EntityModel.of(video, links))
                )
                .collectList()
                .zipWith(videoService.count())
                .map(tuple2 -> new VideoPageGlue(tuple2.getT1(), tuple2.getT2()))
                .zipWith(linkSelf)
                .map(tuple2 -> PagedModel.of(tuple2.getT1().content,
                        new PagedModel.PageMetadata(
                                s,
                                p,
                                tuple2.getT1().totalElements(), tuple2.getT1().totalElements() > s
                                                            ? Math.ceilDiv(tuple2.getT1().totalElements(), s)
                                                            : 1),
                        tuple2.getT2()
                        )
                )
                .map(ResponseEntity::ok);
    }

    /**
     *
     * @param video
     * @param authentication
     * @return flow of {@link Link}
     */
    private Flux<Link> buildVideoLinks(Video video, Authentication authentication) {

        return Flux.just(
                WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(VideoRestController.class)
                                .getById(video.name()))
                        .withRel("getById")
                        .toMono(),
                WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(VideoRestController.class)
                                .deleteByName(video.name()))
                        .withRel("deleteById")
                        .toMono(),
                WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(VideoRestController.class)
                                .create(video, authentication))
                        .withRel("creation")
                        .toMono(),
                WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(VideoRestController.class)
                                .search(video.name()))
                        .withRel("searchByName")
                        .toMono()
        )
        .flatMap(Function.identity());
    }

    record VideoPageGlue(List<EntityModel<Video>> content, long totalElements) {}

    record VideoLinksPageGlue(VideoPageGlue pageGlue, List<Link> links) {}

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
