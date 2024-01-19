package com.springboot.learning.sb3.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.spring.boot.autoconfigure.test.DataOpenSearchTest;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Duration;

@Testcontainers(disabledWithoutDocker = true)
@DataOpenSearchTest
@AutoConfigureJson
@AutoConfigureJsonTesters
@EnableElasticsearchRepositories
@Tag("video-repository-test")
class VideoRepositoryTest {

    @Container
    static final OpensearchContainer<?> opensearch = new OpensearchContainer<>("opensearchproject/opensearch:2.11.1")
            .withStartupAttempts(5)
            .withStartupTimeout(Duration.ofMinutes(2));

    @Autowired VideoRepository videoRepository;
    @Autowired RestHighLevelClient highLevelClient;
    @Autowired ObjectMapper jack;
    ReactiveOpensearchRepository opensearchRepository;

    @BeforeEach
    void setup() {
        opensearchRepository = new ReactiveOpensearchRepository(highLevelClient, jack);
    }

    @Test
    void findAll() {
        Assertions.assertThat(videoRepository.findAll()).isEmpty();
        StepVerifier.create(opensearchRepository.search(new SearchRequest(), VideoEntity.class))
                .expectNextCount(0L)
                .verifyComplete();
    }

    @DynamicPropertySource
    static void dynProps(DynamicPropertyRegistry registry) {
        registry.add("opensearch.uris", opensearch::getHttpHostAddress);
    }
}
