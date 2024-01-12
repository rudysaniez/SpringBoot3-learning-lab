package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.dto.Video;
import com.springboot.learning.sb3.mapper.VideoMapper;
import com.springboot.learning.sb3.repository.VideoRepository;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

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
    @GetMapping(value = "/hal/videos/{videoName}")
    public Mono<EntityModel<Video>> getVideoById(@PathVariable(value = "videoName") String videoName) {

        final Mono<Link> selfLink = WebFluxLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VideoHypermediaController.class).getVideoById(videoName))
                .withSelfRel()
                .toMono();

        final Mono<Link> otherLink = WebFluxLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VideoHypermediaController.class).all())
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
     * @return
     */
    @GetMapping(value = "/hal/videos")
    public Mono<CollectionModel<EntityModel<Video>>> all() {

        final Mono<Link> selfLink = WebFluxLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VideoHypermediaController.class).all())
                .withSelfRel()
                .toMono();

        return selfLink.zipWith(videoRepository.findAll()
                                .map(videoMapper::toModel)
                                .flatMap(video -> Flux.just(
                                        WebFluxLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VideoHypermediaController.class).getVideoById(video.name()))
                                            .withSelfRel()
                                            .toMono(),
                                        WebFluxLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VideoRestController.class).deleteByName(video.name()))
                                            .withRel(LinkRelation.of("delete"))
                                            .toMono()
                                        )
                                        .flatMap(Function.identity())
                                        .collectList()
                                        .map(links -> EntityModel.of(video, links))
                                )
                                .collectList())
                .map(tuple2 -> CollectionModel.of(tuple2.getT2(), tuple2.getT1()));
    }
}
