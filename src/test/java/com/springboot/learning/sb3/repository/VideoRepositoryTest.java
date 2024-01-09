package com.springboot.learning.sb3.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DataR2dbcTest
class VideoRepositoryTest {

    @Autowired VideoRepository videoRepository;

    @ServiceConnection
    @Container
    static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:15.5-alpine")
            .withUsername("michael")
            .withPassword("jordan")
            .withDatabaseName("videoDatabase");

    @BeforeEach
    void setup() {
        Assertions.assertThat(videoRepository).isNotNull();
    }

    @Test
    void findAll() {

        StepVerifier.create(videoRepository.findAll())
                .expectNextCount(3L)
                .verifyComplete();
    }
}
