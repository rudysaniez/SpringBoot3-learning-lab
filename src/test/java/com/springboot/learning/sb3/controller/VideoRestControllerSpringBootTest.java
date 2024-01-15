package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.dto.Video;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VideoRestControllerSpringBootTest {

    @Autowired WebTestClient webTestClient;

    @Test
    void all() {

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("videos").build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath("$.[0].name").isEqualTo("Learn Spring boot 3 (test)")
                .jsonPath("$.[1].name").isEqualTo("Learn Spring boot data jdbc (test)")
                .jsonPath("$.[2].name").isEqualTo("Learn Spring boot data jpa (test)");
    }

    @Test
    void create() {

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.pathSegment("videos").build())
                .headers(httpHeaders -> httpHeaders.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new Video("My video", "My nice vide", "user"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void incorrectCreation() {

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.pathSegment("videos").build())
                .headers(httpHeaders -> httpHeaders.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new Video("", "My nice vide", "user"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void unauthorized() {

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("videos").build())
                .headers(header -> header.setBasicAuth("user", "user1"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
