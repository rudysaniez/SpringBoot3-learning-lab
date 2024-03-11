package com.springboot.learning.repository.tests;

import com.springboot.learning.common.OpensearchHelper;
import com.springboot.learning.repository.impl.ReactiveOpensearchMappingRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.spring.boot.autoconfigure.test.DataOpenSearchTest;
import org.opensearch.testcontainers.OpensearchContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(OutputCaptureExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@DataOpenSearchTest
@AutoConfigureJson
@AutoConfigureJsonTesters
@EnableElasticsearchRepositories
@Tag("attribute-mapping-reactive-repository-test")
class AttributeMappingRepositoryTest {

    @Container
    static final OpensearchContainer<?> opensearch = new OpensearchContainer<>("opensearchproject/opensearch:2.11.1")
            .withStartupAttempts(5)
            .withStartupTimeout(Duration.ofMinutes(2));

    @DynamicPropertySource
    static void dynProps(DynamicPropertyRegistry registry) {
        registry.add("opensearch.uris", opensearch::getHttpHostAddress);
    }

    @Autowired
    RestHighLevelClient highLevelClient;

    ReactiveOpensearchMappingRepository opensearchMappingRepository;

    @Value("classpath:index/index_attribute_dictionary_v1.json")
    Resource indexAttributeDictionaryV1;

    private static final AtomicBoolean INDEX_IS_CREATED = new AtomicBoolean();
    private static final AtomicReference<String> IDX_TARGET = new AtomicReference<>();
    private static final Logger log = LoggerFactory.getLogger(AttributeRepositoryTest.class);

    @BeforeEach
    void setup() {
        opensearchMappingRepository = new ReactiveOpensearchMappingRepository(highLevelClient);

        synchronized (this) {
            if(!INDEX_IS_CREATED.get()) {
                opensearchMappingRepository.createIndex(
                        OpensearchHelper.INDEX_NAME_V1,
                        indexAttributeDictionaryV1,
                        true)
                    .block();
                INDEX_IS_CREATED.set(true);
            }
        }
    }

    @Test
    void getMapping() {
        Map<String, Object> mappings = opensearchMappingRepository.getMapping(OpensearchHelper.INDEX_NAME_V1).block();
        Assertions.assertThat(mappings).isNotEmpty();
    }

    @Test
    void indexNotExist(CapturedOutput output) {

        final var indexName = OpensearchHelper.INDEX_NAME_V1.concat(".0");

        var mapping = opensearchMappingRepository
            .getMapping(indexName)
            .blockOptional();
        Assertions.assertThat(mapping).isEmpty();

        Assertions.assertThat(output.getOut()).contains("type=index_not_found_exception, reason=no such index [attributes_dictionary_v1.0]");
    }

    @Test
    void indexNotExistAndCreatedIt(CapturedOutput output) {

        final var indexName = OpensearchHelper.INDEX_NAME_V1.concat(".1");

        var mapping = opensearchMappingRepository
            .getMapping(indexName)
            .blockOptional();
        Assertions.assertThat(mapping).isEmpty();

        var indexCreated = opensearchMappingRepository.createIndex(indexName, indexAttributeDictionaryV1, true).block();
        Assertions.assertThat(indexCreated).isTrue();

        mapping = opensearchMappingRepository.getMapping(indexName)
            .doOnNext(map -> log.info(" > Mapping is {}.", map))
            .blockOptional();
        Assertions.assertThat(mapping).isPresent();

        Assertions.assertThat(output.getOut())
            .contains("code={fielddata=true, type=text, fields={keyword={ignore_above=256, type=keyword}}}");
    }

    @Test
    void createIndex(CapturedOutput output) {

        final var indexName = OpensearchHelper.INDEX_NAME_V1.concat(".2");

        StepVerifier.create(opensearchMappingRepository
            .createIndex(indexName, indexAttributeDictionaryV1, true)
        )
        .expectNextMatches(result -> result)
        .verifyComplete();

        Assertions.assertThat(output.getOut()).contains("> This index " + indexName + " has been created");
    }

    @Test
    void createIndexAlreadyExists(CapturedOutput output) {

        final var indexName = OpensearchHelper.INDEX_NAME_V1.concat(".3");

        //Index creation
        StepVerifier.create(opensearchMappingRepository.createIndex(indexName,
                        indexAttributeDictionaryV1,
                        true)
        )
        .expectNextMatches(result -> result)
        .verifyComplete();

        Assertions.assertThat(output.getOut()).contains("> This index " + indexName + " has been created");

        //Index already exists
        StepVerifier.create(opensearchMappingRepository
                .createIndex(OpensearchHelper.INDEX_NAME_V1.concat(".3"), indexAttributeDictionaryV1, true)
        )
        .expectNextMatches(result -> !result)
        .verifyComplete();

        Assertions.assertThat(output.getOut()).contains("resource_already_exists_exception");
        Assertions.assertThat(output.getOut()).contains("> This index attributes_dictionary_v1.3 already exists");
    }
}
