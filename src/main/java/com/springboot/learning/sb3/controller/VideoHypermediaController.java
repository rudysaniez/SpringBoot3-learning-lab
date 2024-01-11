package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.dto.Video;
import com.springboot.learning.sb3.mapper.VideoMapper;
import com.springboot.learning.sb3.repository.VideoRepository;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

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

        return videoRepository.findByName(videoName)
                .next()
                .map(videoMapper::toModel)
                .zipWith(selfLink)
                .map(tuple2 -> EntityModel.of(tuple2.getT1(), tuple2.getT2()));
    }

    /**
     *
     * @return
     */
    @GetMapping(value = "/hal/videos")
    public Mono<CollectionModel<Video>> all() {

        final Mono<Link> selfLink = WebFluxLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VideoHypermediaController.class).all())
                .withSelfRel()
                .toMono();


        Mono<CollectionModel<Video>> result =  selfLink.zipWith(videoRepository.findAll().map(videoMapper::toModel).collectList())
                .map(tuple2 -> CollectionModel.of(tuple2.getT2(), tuple2.getT1()));


        return result;

    }
}
