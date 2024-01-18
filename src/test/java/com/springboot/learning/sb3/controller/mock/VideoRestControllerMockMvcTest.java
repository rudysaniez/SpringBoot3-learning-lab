package com.springboot.learning.sb3.controller.mock;

import com.springboot.learning.sb3.controller.VideoRestController;
import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.repository.VideoRepository;
import com.springboot.learning.sb3.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(controllers = VideoRestController.class)
class VideoRestControllerMockMvcTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean VideoService videoService;
    @MockBean VideoRepository videoRepository;

    @BeforeEach
    void setup() {

        final var video1 = new VideoEntity(null, "Learn Spring boot 3 (test)", "Nice framework", "user");
        final var video2 = new VideoEntity(null, "Learn Spring boot data jdbc (test)", "Nice project", "user");
        final var video3 = new VideoEntity(null, "Learn Spring boot data jpa (test)", "Useful project", "user");

        Mockito.when(videoService.findAll())
                .thenReturn(Flux.fromIterable(List.of(video1, video2, video3)));
    }

    @WithMockUser(username = "user")
    @Test
    void all() throws Exception {

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("videos").build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath("$.[0].name").isEqualTo("Learn Spring boot 3 (test)");
                //.jsonPath("$.[1].name").isEqualTo("Learn Spring boot data jdbc (test)")
                //.jsonPath("$.[2].name").isEqualTo("Learn Spring boot data jpa (test)");
    }
}
