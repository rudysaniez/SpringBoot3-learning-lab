package com.adeo.springboot.learning.sb3.controller;

import com.adeo.springboot.learning.sb3.dto.*;
import com.adeo.springboot.learning.sb3.mapper.VideoMapper;
import com.adeo.springboot.learning.sb3.service.VideoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RestController
public class VideoRestController {

    private final VideoService videoService;
    private final VideoMapper videoMapper;

    public VideoRestController(VideoService videoService,
                               VideoMapper videoMapper) {

        this.videoService = videoService;
        this.videoMapper = videoMapper;
    }

    /**
     * Get me all !
     * @return list of {@link Video}
     */
    @GetMapping(value = "/videos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageVideo> all(@RequestParam(value = "page", required = false) Integer page,
                         @RequestParam(value = "size", required = false) Integer size) {

        if(Objects.isNull(page)) page = 0;
        if(Objects.isNull(size)) size = 10;
        if(page < 0) page = 0;
        if(size > 20) size = 20;

        var pagination = videoService.findAll(page, size);

        var videoModelList = StreamSupport.stream(videoService.findAll(page, size).spliterator(), false)
                .map(videoMapper::toModel)
                .toList();

        var pageMetadata = new PageMetadata(pagination.getNumber(),
                pagination.getSize(),
                pagination.getTotalElements(),
                pagination.getTotalPages());

        return ResponseEntity.ok(new PageVideo(videoModelList, pageMetadata));
    }

    /**
     * Count {@link Video}
     * @return number of {@link Video}
     */
    @GetMapping(value = "/videos/:count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(videoService.count());
    }

    /**
     * Search system.
     * @param name : a video search criteria
     * @return list of {@link Video}
     */
    @GetMapping(value = "/videos/:search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Video>> search(@RequestParam(name = "name") String name) {
        return ResponseEntity.ok(
                    videoService.search(new VideoSearch(name, Optional.empty())).stream()
                        .map(videoMapper::toModel)
                        .toList()
        );
    }

    /**
     * Creation of videos.
     * @param video : a video in the request body
     * @return {@link Video}
     */
    @PostMapping(value = "/videos",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Video> create(@RequestBody Video video,
                                        Authentication authentication) {
        return ResponseEntity.ok(videoMapper.toModel(videoService.save(video, authentication.getName())));
    }

    /**
     * Delete videos.
     * @param name : The name of videos to deleted
     * @return {@link List<Video>}
     */
    @DeleteMapping(value = "/videos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteByName(@RequestParam(name = "name") String name,
                                             Authentication authentication) {

        final boolean deleted = videoService.delete(new VideoDeletion(name), authentication);
        if(deleted)
            return ResponseEntity.ok().build();
        else
            return ResponseEntity.noContent().build();
    }
}
