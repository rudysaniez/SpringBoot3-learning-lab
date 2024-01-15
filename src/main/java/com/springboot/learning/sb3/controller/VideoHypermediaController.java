package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.dto.Video;
import com.springboot.learning.sb3.mapper.VideoMapper;
import com.springboot.learning.sb3.repository.VideoRepository;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * <a href="https://docs.spring.io/spring-hateoas/docs/current/reference/html/#fundamentals.link-relations">Hypermedia as the engine of app state</a>
 */
@RestController
public class VideoHypermediaController {

    private final VideoRepository videoRepository;
    private static final VideoMapper videoMapper = Mappers.getMapper(VideoMapper.class);

    public VideoHypermediaController(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    /**
     * @param videoName : the video name
     * @return Mono of {@link EntityModel<Video>}
     */
    @GetMapping(value = "/hypermedia/videos/{videoName}")
    public Mono<EntityModel<Video>> getVideoById(@PathVariable(value = "videoName") String videoName,
                                                 Authentication authentication) {

        final Mono<Link> selfLink = WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder
                                        .methodOn(VideoHypermediaController.class)
                                        .getVideoById(videoName, authentication))
                                    .withSelfRel()
                                    .toMono();

        final Mono<Link> otherLink = WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder
                                        .methodOn(VideoHypermediaController.class)
                                        .all(authentication))
                                    .withRel(LinkRelation.of("all"))
                                    .toMono();

        return Mono.zip(selfLink, otherLink)
                .flatMap(links -> videoRepository.findByName(videoName)
                        .next()
                        .map(videoMapper::toModel)
                        .map(video -> EntityModel.of(video, links.getT1(), links.getT2()))
                );
    }

    /**
     * @return Mono of collection of {@link EntityModel<Video>}
     */
    @GetMapping(value = "/hypermedia/videos")
    public Mono<CollectionModel<EntityModel<Video>>> all(Authentication authentication) {

        final Mono<Link> selfLink = WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder
                                        .methodOn(VideoHypermediaController.class)
                                        .all(authentication))
                                    .withSelfRel()
                                    .toMono();

        return selfLink.zipWith(videoRepository.findAll()
                                .map(videoMapper::toModel)
                                .flatMap(video -> buildVideoLinks(video, authentication)
                                    .collectList()
                                    .map(links -> EntityModel.of(video, links))
                                )
                                .collectList())
                .map(tuple2 -> CollectionModel.of(tuple2.getT2(), tuple2.getT1()));
    }

    /**
     * @param page : the page number
     * @param size : the page size
     * @return flow of page of {@link EntityModel<Video>}
     */
    @GetMapping(value = "/hypermedia/videos/:asPage")
    public Mono<PagedModel<EntityModel<Video>>> allAsPage(@RequestParam(value = "page") Integer page,
                                                          @RequestParam(value = "size") Integer size,
                                                          Authentication authentication) {

        final int p;
        if(Objects.isNull(page))
            p = 0;
        else
            p = page;

        final int s;
        if(Objects.isNull(size))
            s = 10;
        else
            s = size;

        final Mono<Link> selfLink = WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder
                                        .methodOn(VideoHypermediaController.class)
                                        .allAsPage(p, s, authentication))
                                    .withSelfRel()
                                    .toMono();

        return selfLink.zipWith(videoRepository.findAllAsPage(page * size, size)
                            .map(videoMapper::toModel)
                            .flatMap(video -> buildVideoLinks(video, authentication)
                                .collectList()
                                .map(links -> EntityModel.of(video, links))
                            )
                            .collectList()
                            .zipWith(videoRepository.count())
                            .map(tuple2 -> new VideoPageGlue(tuple2.getT1(), tuple2.getT2()))
                )
                .map(tuple2 -> PagedModel.of(tuple2.getT2().videos(),
                                    new PagedModel.PageMetadata(s,
                                            p,
                                            tuple2.getT2().totalElements(),
                                            tuple2.getT2().totalElements() > s ? Math.ceilDiv(tuple2.getT2().totalElements(), s) : 1
                                    ),
                                    tuple2.getT1()))
        ;
    }

    /**
     * @param video : the video model
     * @return flow of {@link Link}
     */
    private Flux<Link> buildVideoLinks(Video video,
                                       Authentication authentication) {

        return Flux.just(
                    WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder
                                    .methodOn(VideoHypermediaController.class)
                                    .getVideoById(video.name(), authentication))
                                .withSelfRel()
                                .toMono(),
                    WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder
                                    .methodOn(VideoRestController.class)
                                    .deleteByName(video.name()))
                                .withRel(LinkRelation.of("delete"))
                                .toMono(),
                    WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder
                                    .methodOn(VideoRestController.class)
                                    .create(video, authentication))
                                .withRel(LinkRelation.of("create"))
                                .toMono()
                )
                .flatMap(Function.identity());
    }

    record VideoPageGlue(List<EntityModel<Video>> videos, long totalElements) {}
}
