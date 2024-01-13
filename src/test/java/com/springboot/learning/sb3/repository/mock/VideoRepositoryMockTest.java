package com.springboot.learning.sb3.repository.mock;

import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.repository.VideoRepository;
import com.springboot.learning.sb3.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class VideoRepositoryMockTest {

    @Mock VideoRepository videoRepository;

    VideoService videoService;

    @BeforeEach
    void setup() {

        final var video1 = new VideoEntity(null, "Learn with Spring-boot 3", "Better framework", "user");
        final var video2 = new VideoEntity(null, "Learn Spring-data-jpa", "Java persistence API", "user");

        Mockito.when(videoRepository.findAll())
                .thenReturn(Flux.fromIterable(List.of(video1, video2)));

        videoService = new VideoService(videoRepository);
    }

    @Test
    void findAll() {

        StepVerifier.create(videoRepository.findAll())
                .expectNextMatches(video -> video.name().equalsIgnoreCase("Learn with Spring-boot 3"))
                .expectNextMatches(video -> video.name().equalsIgnoreCase("Learn Spring-data-jpa"))
                .verifyComplete();
    }
}
