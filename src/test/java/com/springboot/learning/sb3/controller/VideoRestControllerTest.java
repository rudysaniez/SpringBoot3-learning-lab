package com.springboot.learning.sb3.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VideoRestControllerTest {

    @Autowired WebTestClient webTestClient;

    @ServiceConnection
    @Container
    static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:15.5-alpine")
            .withUsername("michael")
            .withPassword("jordan")
            .withDatabaseName("videoDatabase");

    @BeforeEach
    void setup() {
        Assertions.assertThat(webTestClient).isNotNull();
    }

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
}
