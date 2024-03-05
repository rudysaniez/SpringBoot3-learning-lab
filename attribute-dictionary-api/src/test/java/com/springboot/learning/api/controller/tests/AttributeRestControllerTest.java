package com.springboot.learning.api.controller.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.api.controller.contract.v1.AttributeDictionary;
import com.springboot.learning.api.controller.contract.v1.HttpErrorInfo;
import com.springboot.learning.common.JackHelper;
import com.springboot.learning.common.OpensearchHelper;
import com.springboot.learning.common.WaitHelper;
import com.springboot.learning.dictionary.domain.AttributeDictionaryEntity;
import com.springboot.learning.service.impl.AttributeDictionaryService;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensearch.testcontainers.OpensearchContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.atomic.AtomicBoolean;

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

    static final AtomicBoolean INDEX_IS_CREATED = new AtomicBoolean();

    static final Logger log = LoggerFactory.getLogger(AttributeRestControllerTest.class);

    @BeforeEach
    void setup() {

        synchronized (this) {
            if(!INDEX_IS_CREATED.get()) {
                var result = OpensearchHelper.putIndexV1(opensearch.getHttpHostAddress());
                result.ifPresent(openSearchIndexCreationResult -> INDEX_IS_CREATED.set(openSearchIndexCreationResult.acknowledged()));
            }
        }

        filling(attributes);
    }

    @AfterEach
    void after() {
        clean();
    }

    @Tag("Get an attribute by identifier")
    @Test
    void getAttributeById() {

        var attribute = attributeDictionaryService.searchWithQueryPrefix("code", "code01", 1)
                .next()
                .block();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("attributes", attribute.id()).build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);
    }

    @Tag("Get no attributes by identifier because it doesn't exist")
    @Test
    void getNoAttributesByNotExistIdentifier() {

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("attributes", "not_exist_identifier").build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Tag("Retrieve attributes as page")
    @Test
    void getAttributesAsPage() {

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("attributes").build())
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
                .uri(uriBuilder -> uriBuilder.pathSegment("attributes", ":search")
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
        final var attr = JackHelper.getAttributeCandidate(jack, attribute01, AttributeDictionary.class);
        Assertions.assertThat(attr).isNotNull();

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.pathSegment("attributes").build())
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
        final var attr = JackHelper.getAttributeCandidate(jack, attributeBad01, AttributeDictionary.class);
        Assertions.assertThat(attr).isNotNull();

        Mono<HttpErrorInfo> httpErrorInfoMono = webTestClient.post()
                .uri(uriBuilder -> uriBuilder.pathSegment("attributes").build())
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
                        && httpErrorInfo.path().equalsIgnoreCase("/attributes")
                )
                .verifyComplete();
    }

    @Tag("Save on attribute asynchronously")
    @Test
    void saveAsync() throws IOException {

        // Get an attribute.
        final var attr = JackHelper.getAttributeCandidate(jack, attribute01, AttributeDictionary.class);
        Assertions.assertThat(attr).isNotNull();

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.pathSegment("attributes", ":async").build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(attr)
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.ACCEPTED);
    }

    @Tag("Update an attribute")
    @Test
    void update() throws IOException {

        final var attributeModel = JackHelper.getAttributeCandidate(jack, attribute01Up, AttributeDictionary.class);

        final var attributeEntity = attributeDictionaryService.searchWithQueryPrefix("code", "code01", 1)
                .next()
                .block();

        webTestClient.put()
                .uri(uri -> uri.pathSegment("attributes", attributeEntity.id()).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(attributeModel)
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath("$.referenceDataName").isEqualTo("REF_NAME_0100");
    }

    @Tag("Delete an attribute")
    @Test
    void deleteOne() {

        final var attribute = attributeDictionaryService.searchWithQueryPrefix("code", "code01", 1)
                .next()
                .block();

        webTestClient.delete()
                .uri(uri -> uri.pathSegment("attributes", attribute.id()).build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);

        WaitHelper.waitInSecond(2);

        final var attributeAfterDelete = attributeDictionaryService.searchWithQueryPrefix("code", "code01", 1)
                .next()
                .blockOptional();
        Assertions.assertThat(attributeAfterDelete).isEmpty();
    }

    @Tag("Delete all attributes")
    @Test
    void deleteAll() {

        var count = webTestClient.delete()
                .uri(uri -> uri.pathSegment("attributes", ":empty").build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .returnResult(Long.class)
                .getResponseBody()
                .next();

        StepVerifier.create(count)
                .expectNextMatches(aLong -> aLong.equals(5L))
                .verifyComplete();

        WaitHelper.waitInSecond(2);

        final var attributeAfterDelete = attributeDictionaryService.searchWithQueryPrefix("code", "code", 10)
                .collectList()
                .blockOptional();
        Assertions.assertThat(attributeAfterDelete).isPresent();
        Assertions.assertThat(attributeAfterDelete.get()).isEmpty();
    }

    /**
     * @param input : the input resource
     */
    private void filling(Resource input) {

        final List<AttributeDictionaryEntity> entities = JackHelper.getManyAttributeCandidates(jack, input);
        Assertions.assertThat(entities).isNotEmpty();

        var disposable = attributeDictionaryService.bulk(entities)
                .subscribe(crudResult -> log.info(" > Create an attribute, result is {}", crudResult));
        Awaitility.await().until(disposable::isDisposed);

        WaitHelper.waitInSecond(1);
    }

    private void clean() {
        WaitHelper.waitInSecond(1);
        var result = attributeDictionaryService.deleteAll().blockOptional();
        result.ifPresent(count -> log.info(" > {} attributes deleted", count));
    }
}
