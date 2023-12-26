package com.adeo.springboot.learning.sb3.service;

import com.adeo.springboot.learning.sb3.domain.VideoEntity;
import com.adeo.springboot.learning.sb3.dto.VideoDeletion;
import com.adeo.springboot.learning.sb3.dto.VideoSearch;
import com.adeo.springboot.learning.sb3.mapper.VideoMapper;
import com.adeo.springboot.learning.sb3.repository.VideoPagingRepository;
import com.adeo.springboot.learning.sb3.repository.VideoRepository;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
     * @return {@link Page<  VideoEntity  >}
     */
    public Page<VideoEntity> findAll(int page, int size) {
        return videoPagingRepository.findAll(PageRequest.of(page, size));
    }

    /**
     * @return number of {@link VideoEntity}
     */
    public long count() {
        return videoRepository.count();
    }

    /**
     * @param video : a video
     * @return {@link VideoEntity}
     */
    public VideoEntity save(@NotNull com.adeo.springboot.learning.sb3.dto.Video video) {
        return videoRepository.save(videoMapper.toEntity(video));
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
    public boolean delete(VideoDeletion videoDeletion) {

        log.info(" > Delete all by {}.", videoDeletion);

        int videoDeletedNumber = videoRepository.deleteByName(videoDeletion.name());
        if(videoDeletedNumber > 0)
            log.info(" > Number of video deleted is {}.", videoDeletedNumber);
        else
            log.info(" > Nothing deletion is executed.");

        return videoDeletedNumber > 0;
    }
}
