package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.dto.Video;
import com.springboot.learning.sb3.dto.VideoDeletion;
import com.springboot.learning.sb3.dto.VideoSearch;
import com.springboot.learning.sb3.exception.InvalidInputException;
import com.springboot.learning.sb3.mapper.VideoMapper;
import com.springboot.learning.sb3.service.VideoService;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

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
                .defaultIfEmpty(ResponseEntity.noContent().build());
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
            p = page < 0 ? 0 : page;

        if(Objects.isNull(size))
            s = 10;
        else
            s = size > 10 ? 10 : size;

        return videoService.findAll()
                .map(videoMapper::toModel);
    }

    /**
     * Get me all !
     * @return list of {@link Video}
     */
    @GetMapping(value = "/videos/:asPage")
    public Mono<ResponseEntity<PagedModel<EntityModel<Video>>>> allAsPage(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            Authentication authentication) {

        final int p;
        final int s;

        if(Objects.isNull(page))
            p = 0;
        else
            p = page < 0 ? 0 : page;

        if(Objects.isNull(size))
            s = 20;
        else
            s = size > 10 ? 10 : size;

        log.info(" > All videos with page by the user {}.", authentication.getName());

        final Mono<Link> linkSelf = WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(VideoRestController.class)
                                    .allAsPage(p, s, authentication))
                                    .withSelfRel()
                                    .toMono();

        return videoService.findAll()
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
     * @param video : the video
     * @param authentication : the authentication
     * @return flow of {@link Link}
     */
    private Flux<Link> buildVideoLinks(Video video, Authentication authentication) {

        return Flux.just(
                WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(VideoRestController.class)
                                .getById(video.videoName()))
                        .withRel(LinkRelation.of("get"))
                        .toMono(),
                WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(VideoRestController.class)
                                .deleteByName(video.videoName(), authentication))
                        .withRel(LinkRelation.of("delete"))
                        .toMono(),
                WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(VideoRestController.class)
                                .search(video.videoName()))
                        .withRel(LinkRelation.of("searchByName"))
                        .toMono()
        )
        .flatMap(Function.identity());
    }

    record VideoPageGlue(List<EntityModel<Video>> content, long totalElements) {}

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

        return videoService.searchByNameContaining(name)
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

        if(!StringUtils.hasText(video.videoName()))
            throw new InvalidInputException("The video is incorrect, the video name is missing.");

        return videoService.save(video, authentication.getName())
                .map(videoMapper::toModel)
                .map(ResponseEntity::ok);
        /*
        return videoService.findByName(video.videoName())
                .collectList()
                .<Video>handle((videos, sink) -> {
                    if(videos.isEmpty())
                        sink.next(video);
                })
                .switchIfEmpty(Mono.error(new InvalidInputException("The video already exist.")))
                .flatMap(v -> videoService.save(v, authentication.getName()))
                .map(videoMapper::toModel)
                .map(v -> new ResponseEntity<>(v, HttpStatus.CREATED));
         */
    }

    /**
     * Delete videos.
     * @param name : The name of videos to deleted
     * @return {@link List<Video>}
     */
    @DeleteMapping(value = "/videos", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Video>> deleteByName(@RequestParam(name = "name") String name,
                                                    Authentication authentication) {

        if(!StringUtils.hasText(name))
            throw new IllegalArgumentException(" > The query parameter name is mandatory");

        return videoService.delete(new VideoDeletion(name), authentication)
                .map(videoMapper::toModel)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.noContent().build()));
    }

    /**
     * Delete videos.
     * @param videoId : The name of videos to deleted
     * @return {@link List<Video>}
     */
    @DeleteMapping(value = "/videos/{videoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Video>> deleteById(@PathVariable(name = "videoId") String videoId,
                                                 Authentication authentication) {

        return videoService.delete(new VideoDeletion(videoId), authentication)
                .map(videoMapper::toModel)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.noContent().build()));
    }
}
