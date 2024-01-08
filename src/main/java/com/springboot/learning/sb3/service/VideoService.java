package com.springboot.learning.sb3.service;

import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.dto.VideoDeletion;
import com.springboot.learning.sb3.dto.Video;
import com.springboot.learning.sb3.dto.VideoSearch;
import com.springboot.learning.sb3.mapper.VideoMapper;
import com.springboot.learning.sb3.repository.VideoPagingRepository;
import com.springboot.learning.sb3.repository.VideoRepository;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final VideoPagingRepository videoPagingRepository;
    private final VideoMapper videoMapper;

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    public VideoService(
            VideoRepository videoRepository,
            VideoPagingRepository videoPagingRepository,
            VideoMapper videoMapper) {

        this.videoRepository = videoRepository;
        this.videoPagingRepository = videoPagingRepository;
        this.videoMapper = videoMapper;
    }

    /**
     * @param page : the page number
     * @param size : the size
     * @return page of {@link VideoEntity}
     */
    public Page<VideoEntity> findAll(int page, int size) {
        return videoPagingRepository.findAll(PageRequest.of(page, size));
    }

    /**
     * @param name : the video name
     * @return {@link List<VideoEntity>}
     */
    public List<VideoEntity> findByName(String name) {
        return videoRepository.findByName(name);
    }

    /**
     * @return number of {@link VideoEntity}
     */
    public long count() {
        return videoRepository.count();
    }

    /**
     * @param video : a video
     * @param username : the user
     * @return {@link VideoEntity}
     */
    public VideoEntity save(@NotNull Video video,
                            @NotNull String username) {

        final VideoEntity entity = new VideoEntity(null, video.name(), video.description(), username);
        return videoRepository.save(entity);
    }

    /**
     * @param videoSearch : a video search criteria
     * @return list of {@link VideoEntity}
     */
    public List<VideoEntity> search(VideoSearch videoSearch) {

        log.info(" > Search many videos by {}.", videoSearch);

        if(StringUtils.hasText(videoSearch.name()))
            return videoRepository.findTop3ByNameContainsIgnoreCaseOrderByName(videoSearch.name());
        else if (videoSearch.description().isPresent() && StringUtils.hasText(videoSearch.description().get()))
            return videoRepository.findTop3ByDescriptionContainingIgnoreCaseOrderByName(videoSearch.description().get());

        return List.of();
    }

    /**
     * Delete by {@link VideoDeletion}.
     * @param videoDeletion : the video deletion
     */
    public boolean delete(@NotNull VideoDeletion videoDeletion,
                          Authentication authentication) {

        log.info(" > Delete this video {}, by {}.", videoDeletion, authentication.getName());

        List<VideoEntity> data = this.findByName(videoDeletion.name());

        final boolean out;

        if(!data.isEmpty()) {
            data.forEach(videoRepository::delete);
            out = true;
        }
        else out = false;

        return out;
    }
}
