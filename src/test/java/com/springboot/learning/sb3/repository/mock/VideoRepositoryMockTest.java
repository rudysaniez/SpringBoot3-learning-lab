package com.springboot.learning.sb3.repository.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.repository.VideoRepository;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import com.springboot.learning.sb3.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.RestHighLevelClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class VideoRepositoryMockTest {

    @Mock VideoRepository videoRepository;
    @Mock RestHighLevelClient highLevelClient;
    @Mock ObjectMapper jack;
    @Mock ReactiveOpensearchRepository opensearchRepository;

    VideoService videoService;

    @BeforeEach
    void setup() {

        final var video1 = new VideoEntity(null, "Learn with Spring-boot 3", "Better framework", "user");
        final var video2 = new VideoEntity(null, "Learn Spring-data-jpa", "Java persistence API", "user");

        Mockito.when(videoRepository.findAll())
                .thenReturn(null);

        videoService = new VideoService(videoRepository, highLevelClient, jack, opensearchRepository);
    }

    @Test
    void findAll() {

//        StepVerifier.create(videoRepository.findAll())
//                .expectNextMatches(video -> video.name().equalsIgnoreCase("Learn with Spring-boot 3"))
//                .expectNextMatches(video -> video.name().equalsIgnoreCase("Learn Spring-data-jpa"))
//                .verifyComplete();
    }
}
