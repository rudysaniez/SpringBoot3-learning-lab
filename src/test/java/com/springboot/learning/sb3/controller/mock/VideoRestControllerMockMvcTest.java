package com.springboot.learning.sb3.controller.mock;

import com.springboot.learning.sb3.controller.VideoRestController;
import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.dto.Video;
import com.springboot.learning.sb3.mapper.VideoMapper;
import com.springboot.learning.sb3.repository.UserAccountRepository;
import com.springboot.learning.sb3.repository.VideoRepository;
import com.springboot.learning.sb3.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(controllers = VideoRestController.class)
class VideoRestControllerMockMvcTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean VideoService videoService;
    @MockBean UserAccountRepository userAccountRepository;
    @MockBean VideoRepository videoRepository;

    @BeforeEach
    void setup() {

        final var video1 = new VideoEntity(null, "Learn Spring boot 3 (test)", "Nice framework", "user");
        final var video2 = new VideoEntity(null, "Learn Spring boot data jdbc (test)", "Nice project", "user");
        final var video3 = new VideoEntity(null, "Learn Spring boot data jpa (test)", "Useful project", "user");

        Mockito.when(videoService.findAll(0, 20))
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
