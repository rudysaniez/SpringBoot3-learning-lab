package com.springboot.learning.sb3.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.helper.TestHelper;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.spring.boot.autoconfigure.test.DataOpenSearchTest;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StringUtils;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Testcontainers(disabledWithoutDocker = true)
@DataOpenSearchTest
@AutoConfigureJson
@AutoConfigureJsonTesters
@EnableElasticsearchRepositories
@Tag("attribute-reactive-repository-test")
class AttributeRepositoryTest {

    @Container
    static final OpensearchContainer<?> opensearch = new OpensearchContainer<>("opensearchproject/opensearch:2.11.1")
            .withStartupAttempts(5)
            .withStartupTimeout(Duration.ofMinutes(2));

    @DynamicPropertySource
    static void dynProps(DynamicPropertyRegistry registry) {
        registry.add("opensearch.uris", opensearch::getHttpHostAddress);
    }

    @Autowired RestHighLevelClient highLevelClient;
    @Autowired ObjectMapper jack;

    ReactiveOpensearchRepository opensearchRepository;

    @Value("classpath:json/attribute01.json")
    Resource attribute01;

    private static final AtomicBoolean INDEX_IS_CREATED = new AtomicBoolean();
    private static final String IDX_TARGET = "attribute_dictionary_v1";

    @BeforeEach
    void setup() {
        opensearchRepository = new ReactiveOpensearchRepository(highLevelClient, jack);

        synchronized (this) {
            if(!INDEX_IS_CREATED.get()) {
                var result = TestHelper.putIndexV1(opensearch.getHttpHostAddress());
                result.ifPresent(openSearchIndexCreationResult -> INDEX_IS_CREATED.set(openSearchIndexCreationResult.acknowledged()));
            }
        }
    }

    @Test
    void getById() throws IOException {

        // Get an attribute.
        final AttributeDictionaryEntity entity = jack.readValue(attribute01.getInputStream(), AttributeDictionaryEntity.class);
        Assertions.assertThat(entity).isNotNull();

        // Persist it.
        final AtomicReference<AttributeDictionaryEntity> justAfterSaved = new AtomicReference<>();
        StepVerifier.create(opensearchRepository.save(IDX_TARGET, entity, AttributeDictionaryEntity.class)
                        .doOnNext(justAfterSaved::set))
                .expectNextMatches(attrEntity -> StringUtils.hasText(attrEntity.id())
                                                     && attrEntity.id().equals(justAfterSaved.get().id()))
                .verifyComplete();

        //Get by Identifier.
        StepVerifier.create(opensearchRepository.getById(IDX_TARGET, justAfterSaved.get().id(),
                                AttributeDictionaryEntity.class))
                .expectNextMatches(attrEntity -> attrEntity.id().equals(justAfterSaved.get().id()))
                .verifyComplete();

        //Delete it.
        StepVerifier.create(opensearchRepository.delete(IDX_TARGET, justAfterSaved.get().id()))
                .expectNextMatches(status -> status.equals(200))
                .verifyComplete();

    }
}
