package com.springboot.learning.sb3.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.controller.contract.AttributeDictionary;
import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.helper.TestHelper;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
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
import java.util.List;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestChannelBinderConfiguration.class})
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
    @Autowired ReactiveOpensearchRepository opensearchRepository;

    @Value("classpath:json/attribute01.json")
    Resource attribute01;

    @Value("classpath:json/attributes.json")
    Resource attributes;

    @BeforeEach
    void setup() {}

    @Tag("Retrieve attributes on a empty index")
    @Test
    void all() throws IOException {

        filling(attributes);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("v1", "attributes").build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(5);

        TestHelper.clean(webTestClient, "v1", "attributes", ":empty");
    }

    @Tag("Save one attribute")
    @Test
    void save() throws IOException {

        // Get an attribute.
        final var attr = TestHelper.getAttributeCandidate(jack, attribute01, AttributeDictionary.class);
        Assertions.assertThat(attr).isNotNull();

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.pathSegment("v1", "attributes").build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(attr)
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CODE01")
                .jsonPath("$.group").isEqualTo("GROUP01")
                .jsonPath("$.metricFamily").isEqualTo("METRIC01");

        TestHelper.waitInSecond(1);
        TestHelper.clean(webTestClient, "v1", "attributes", ":empty");
    }

    @Tag("Save on attribute asynchronously")
    @Test
    void saveAsync() throws IOException {

        // Get an attribute.
        final var attr = TestHelper.getAttributeCandidate(jack, attribute01, AttributeDictionary.class);
        Assertions.assertThat(attr).isNotNull();

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.pathSegment("v1", "attributes", ":async").build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(attr)
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.ACCEPTED);

        TestHelper.waitInSecond(1);
        TestHelper.clean(webTestClient, "v1", "attributes", ":empty");
    }

    @Tag("Save many attributes")
    @Test
    void bulk() throws IOException {

        final List<AttributeDictionary> entities = getManyAttributeCandidates(jack, attributes);
        Assertions.assertThat(entities).isNotEmpty();

        webTestClient.post()
                .uri(uri -> uri.pathSegment("v1", "attributes", ":bulk").build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(entities)
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(5)
                .jsonPath("$.[0].status").isEqualTo(201)
                .jsonPath("$.[1].status").isEqualTo(201)
                .jsonPath("$.[2].status").isEqualTo(201)
                .jsonPath("$.[3].status").isEqualTo(201)
                .jsonPath("$.[4].status").isEqualTo(201);

        TestHelper.waitInSecond(1);
        TestHelper.clean(webTestClient, "v1", "attributes", ":empty");
    }

    @Tag("Save many attributes asynchronously")
    @Test
    void bulkAsync() throws IOException {

        final List<AttributeDictionary> entities = getManyAttributeCandidates(jack, attributes);
        Assertions.assertThat(entities).isNotEmpty();

        webTestClient.post()
                .uri(uri -> uri.pathSegment("v1", "attributes", ":bulk-async").build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(entities)
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.ACCEPTED);

        TestHelper.waitInSecond(1);
        TestHelper.clean(webTestClient, "v1", "attributes", ":empty");
    }

    @Tag("Update an attribute")
    @Test
    void update() throws IOException {

        filling(attributes);

        // Launch a search by the code
        final var query = SearchSourceBuilder.searchSource()
                .query(QueryBuilders.matchQuery("code", "CODE01"))
                .from(0)
                .size(2);
        final var request = new SearchRequest(new String[]{AttributeDictionaryRestController.IDX_TARGET}, query);
        final var searchResult = opensearchRepository.search(request, AttributeDictionaryEntity.class)
                .collectList().block();
        Assertions.assertThat(searchResult).isNotEmpty();

        // Update an attribute
        var attribute = searchResult.stream().findFirst().get();
        attribute.setGroup("GROUP_10");

        webTestClient.put()
                .uri(uri -> uri.pathSegment("v1", "attributes", attribute.getId()).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(attribute)
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath("$.group").isEqualTo("GROUP_10");

        TestHelper.clean(webTestClient, "v1", "attributes", ":empty");
    }

    private void filling(Resource input) throws IOException {

        final List<AttributeDictionary> entities = getManyAttributeCandidates(jack, input);
        Assertions.assertThat(entities).isNotEmpty();

        webTestClient.post()
                .uri(uri -> uri.pathSegment("v1", "attributes", ":bulk-async").build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(entities)
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.ACCEPTED);

        TestHelper.waitInSecond(2);
    }

    private List<AttributeDictionary> getManyAttributeCandidates(ObjectMapper jack, Resource input) throws IOException {
        return jack.readValue(input.getContentAsByteArray(), new TypeReference<>() {});
    }
}
