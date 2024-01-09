package com.springboot.learning.sb3.repository.mock;

import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.mapper.VideoMapper;
import com.springboot.learning.sb3.repository.VideoPagingRepository;
import com.springboot.learning.sb3.repository.VideoRepository;
import com.springboot.learning.sb3.service.VideoService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class VideoRepositoryMockTest {

    @Mock
    VideoPagingRepository videoPagingRepository;
    @Mock
    VideoRepository videoRepository;
    @Mock VideoMapper videoMapper;

    VideoService videoService;

    @BeforeEach
    void setup() {

        final var video1 = new VideoEntity(null, "Learn with Spring-boot 3", "Better framework", "user");
        final var video2 = new VideoEntity(null, "Learn Spring-data-jpa", "Java persistence API", "user");

        Mockito.when(videoPagingRepository.findAll(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(video1, video2), PageRequest.of(0, 20), 2L));

        videoService = new VideoService(videoRepository, videoPagingRepository, videoMapper);
    }

    @Test
    void findAll() {

        final Page<VideoEntity> page = videoPagingRepository.findAll(PageRequest.of(0, 20));
        Assertions.assertThat(page.getContent()).hasSize(2);
        Assertions.assertThat(page.getTotalPages()).isEqualTo(1);
        Assertions.assertThat(page.getSize()).isEqualTo(20);
        Assertions.assertThat(page.getNumber()).isZero();
        Assertions.assertThat(page.getTotalElements()).isEqualTo(2);
    }
}
