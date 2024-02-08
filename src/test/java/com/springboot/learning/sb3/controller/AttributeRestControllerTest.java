package com.springboot.learning.sb3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.controller.contract.v1.AttributeDictionary;
import com.springboot.learning.sb3.controller.contract.v1.HttpErrorInfo;
import com.springboot.learning.sb3.helper.TestHelper;
import com.springboot.learning.sb3.service.v1.AttributeDictionaryService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
    @Autowired AttributeDictionaryService attributeDictionaryService;

    @Value("classpath:json/attribute01.json")
    Resource attribute01;

    @Value("classpath:json/attribute01Up.json")
    Resource attribute01Up;

    @Value("classpath:json/attributes.json")
    Resource attributes;

    @Value("classpath:json/attributeBad01.json")
    Resource attributeBad01;

    @BeforeEach
    void setup() {
        filling(attributes);
    }

    @AfterEach
    void after() {
        TestHelper.clean(webTestClient, "v1", "attributes", ":empty");
    }

    @Tag("Get an attribute by identifier")
    @Test
    void getAttributeById() {

        var attribute = attributeDictionaryService.searchWithQueryPrefix("code", "code01", 1)
                .next()
                .block();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("v1", "attributes", attribute.id()).build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);
    }

    @Tag("Get no attributes by identifier because it doesn't exist")
    @Test
    void getNoAttributesByNotExistIdentifier() {

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("v1", "attributes", "not_exist_identifier").build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Tag("Retrieve attributes as page")
    @Test
    void getAttributesAsPage() {

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("v1", "attributes").build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(5);
    }

    @Tag("Search attributes")
    @Test
    void searchAttributes() {

        var attributesFlux = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("v1", "attributes", ":search")
                        .queryParam("q", "code=code01")
                        .build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .returnResult(new ParameterizedTypeReference<List<AttributeDictionary>>() {})
                .getResponseBody()
                .next()
                .flatMapIterable(attributeDictionaries -> attributeDictionaries);

        StepVerifier.create(attributesFlux)
                .expectNextMatches(attributeDictionary -> attributeDictionary.code().equals("CODE01"))
                .verifyComplete();
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
    }

    @Tag("Save one bad attribute")
    @Test
    void saveBad() throws IOException {

        // Get an attribute.
        final var attr = TestHelper.getAttributeCandidate(jack, attributeBad01, AttributeDictionary.class);
        Assertions.assertThat(attr).isNotNull();

        Mono<HttpErrorInfo> httpErrorInfoMono = webTestClient.post()
                .uri(uriBuilder -> uriBuilder.pathSegment("v1", "attributes").build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(attr)
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .returnResult(HttpErrorInfo.class)
                .getResponseBody()
                .next();

        StepVerifier.create(httpErrorInfoMono)
                .expectNextMatches(httpErrorInfo -> httpErrorInfo.httpStatus().equals(HttpStatus.UNPROCESSABLE_ENTITY)
                        && httpErrorInfo.message().equalsIgnoreCase("The code field in attribute dictionary is mandatory")
                        && httpErrorInfo.path().equalsIgnoreCase("/v1/attributes")
                )
                .verifyComplete();
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
    }

    @Tag("Bulk many attributes")
    @Test
    void bulk() {

        final List<AttributeDictionary> entities = TestHelper.getManyAttributeCandidates(jack, attributes);
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

    @Tag("Bulk many attributes asynchronously")
    @Test
    void bulkAsync() {

        final List<AttributeDictionary> entities = TestHelper.getManyAttributeCandidates(jack, attributes);
        Assertions.assertThat(entities).isNotEmpty();

        webTestClient.post()
                .uri(uri -> uri.pathSegment("v1", "attributes", ":bulk-async").build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(entities)
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.ACCEPTED);
    }

    @Tag("Update an attribute")
    @Test
    void update() throws IOException {

        final var attributeModel = TestHelper.getAttributeCandidate(jack, attribute01Up, AttributeDictionary.class);

        final var attributeEntity = attributeDictionaryService.searchWithQueryPrefix("code", "code01", 1)
                .next()
                .block();

        webTestClient.put()
                .uri(uri -> uri.pathSegment("v1", "attributes", attributeEntity.id()).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(attributeModel)
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath("$.referenceDataName").isEqualTo("REF_NAME_0100");
    }

    @Test
    void deleteOne() {

        final var attribute = attributeDictionaryService.searchWithQueryPrefix("code", "code01", 1)
                .next()
                .block();

        webTestClient.delete()
                .uri(uri -> uri.pathSegment("v1", "attributes", attribute.id()).build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);
    }

    /**
     * @param input : the input resource
     */
    private void filling(Resource input) {

        final List<AttributeDictionary> entities = TestHelper.getManyAttributeCandidates(jack, input);
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
}
