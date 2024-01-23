package com.springboot.learning.sb3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("attribute-reactive-web-test")
class AttributeRestControllerTest {

    @Container
    static final OpensearchContainer<?> opensearch = new OpensearchContainer<>("opensearchproject/opensearch:2.11.1")
            .withStartupAttempts(5)
            .withStartupTimeout(Duration.ofMinutes(2));

    @DynamicPropertySource
    static void dynProps(DynamicPropertyRegistry registry) {
        registry.add("opensearch.uris", opensearch::getHttpHostAddress);
    }

    @Autowired WebTestClient webTestClient;

    @Autowired ObjectMapper jack;

    @Value("classpath:json/attribute01.json")
    Resource attribute01;

    @BeforeEach
    void setup() {
        Assertions.assertThat(webTestClient).isNotNull();
    }

    @Test
    void all() {

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("v1", "attributes").build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void save() throws IOException {

        // Get an attribute.
        final AttributeDictionaryEntity entity = jack.readValue(attribute01.getInputStream(), AttributeDictionaryEntity.class);
        Assertions.assertThat(entity).isNotNull();

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.pathSegment("v1", "attributes").build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(entity), AttributeDictionaryEntity.class)
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED);

        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder.pathSegment("v1", "attributes", ":empty").build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);
    }
}
