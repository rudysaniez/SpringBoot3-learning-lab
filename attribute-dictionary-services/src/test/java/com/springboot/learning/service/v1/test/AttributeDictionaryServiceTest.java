package com.springboot.learning.service.v1.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.repository.domain.AttributeDictionaryEntity;
import com.springboot.learning.repository.impl.ReactiveOpensearchRepository;
import com.springboot.learning.service.contract.impl.v1.AttributeDictionaryService;
import com.springboot.learning.service.test.helper.TestHelper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.spring.boot.autoconfigure.test.DataOpenSearchTest;
import org.opensearch.testcontainers.OpensearchContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.core.io.Resource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StringUtils;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Testcontainers(disabledWithoutDocker = true)
@DataOpenSearchTest
@AutoConfigureJson
@AutoConfigureJsonTesters
@Tag("attribute-dictionary-service-v1-test")
class AttributeDictionaryServiceTest {

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
    AttributeDictionaryService attributeDictionaryService;

    @Value("classpath:json/attribute01.json")
    Resource attribute01;

    @Value("classpath:json/attribute01Up.json")
    Resource attribute01Up;

    @Value("classpath:json/attributes.json")
    Resource attributes;

    private static final AtomicBoolean INDEX_IS_CREATED = new AtomicBoolean();
    private static final String IDX_TARGET = "attribute_dictionary_v1";
    private static final Logger log = LoggerFactory.getLogger(AttributeDictionaryServiceTest.class);

    @BeforeEach
    void setup() {
        opensearchRepository = new ReactiveOpensearchRepository(highLevelClient, jack);
        attributeDictionaryService = new AttributeDictionaryService(opensearchRepository);

        synchronized (this) {
            if(!INDEX_IS_CREATED.get()) {
                var result = TestHelper.putIndexV1(opensearch.getHttpHostAddress());
                result.ifPresent(openSearchIndexCreationResult -> INDEX_IS_CREATED.set(openSearchIndexCreationResult.acknowledged()));
            }
        }
    }

    @AfterEach
    void after() {
        TestHelper.waitInSecond(1);
        var deletedNumber = attributeDictionaryService.deleteAll().block();
        log.info(" > Delete all elements {}.", deletedNumber);
    }

    @Tag(value = "Get an attribute by identifier")
    @Test
    void getAttributeById() throws IOException {

        // Get an attribute.
        final AttributeDictionaryEntity entity = jack.readValue(attribute01.getInputStream(), AttributeDictionaryEntity.class);
        Assertions.assertThat(entity).isNotNull();

        // Persist it.
        final AtomicReference<AttributeDictionaryEntity> justAfterSaved = new AtomicReference<>();
        StepVerifier.create(attributeDictionaryService.save(entity)
                        .doOnNext(justAfterSaved::set))
                .expectNextMatches(attrEntity -> StringUtils.hasText(attrEntity.id())
                                                     && attrEntity.id().equals(justAfterSaved.get().id()))
                .verifyComplete();

        //Get by Identifier.
        StepVerifier.create(attributeDictionaryService.getAttributeById(justAfterSaved.get().id()))
                .expectNextMatches(attrEntity -> attrEntity.id().equals(justAfterSaved.get().id()))
                .verifyComplete();
    }

    @Test
    void bulk() {

        List<AttributeDictionaryEntity> entities = TestHelper.getManyAttributeCandidates(jack, attributes);

        Flux<ReactiveOpensearchRepository.CrudResult> entityFlux = attributeDictionaryService.bulk(entities);

        StepVerifier.create(entityFlux)
                .expectNextMatches(crudResult -> crudResult.status() == 201)
                .expectNextMatches(crudResult -> crudResult.status() == 201)
                .expectNextMatches(crudResult -> crudResult.status() == 201)
                .expectNextMatches(crudResult -> crudResult.status() == 201)
                .expectNextMatches(crudResult -> crudResult.status() == 201)
                .verifyComplete();
    }

    @Test
    void deleteOne() throws IOException {

        //Prepare an attribute
        final AttributeDictionaryEntity entity = jack.readValue(attribute01.getInputStream(), AttributeDictionaryEntity.class);
        Assertions.assertThat(entity).isNotNull();

        var attributeCreated = attributeDictionaryService.save(entity).block();

        //Delete it.
        StepVerifier.create(attributeDictionaryService.deleteOne(attributeCreated.id()))
                .expectNextMatches(status -> status.equals(200))
                .verifyComplete();
    }

    @Test
    void save() throws IOException {

        //Prepare an attribute
        final AttributeDictionaryEntity entity = jack.readValue(attribute01.getInputStream(), AttributeDictionaryEntity.class);
        Assertions.assertThat(entity).isNotNull();

        var entityMono = attributeDictionaryService.save(entity);
        StepVerifier.create(entityMono)
                .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals("CODE01"))
                .verifyComplete();
    }

    @Test
    void update() throws IOException {

        save();
        TestHelper.waitInSecond(1);

        var attributeIdFound = attributeDictionaryService.searchNoPredicateButLimited(1)
                .next()
                .map(AttributeDictionaryEntity::id)
                .block();

        //Get attribute candidate
        var attributeCandidate = TestHelper.getAttributeCandidate(jack, attribute01Up, AttributeDictionaryEntity.class);

        //Launch the update
        Mono<AttributeDictionaryEntity> attributeUpdated = attributeDictionaryService.update(
                attributeIdFound,
                attributeCandidate);

        StepVerifier.create(attributeUpdated)
                .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity
                                        .referenceDataName().equals("REF_NAME_0100"))
                .verifyComplete();
    }

    @Test
    void search() {

        bulk();
        TestHelper.waitInSecond(1);

        final SearchSourceBuilder query = SearchSourceBuilder.searchSource()
                .query(QueryBuilders.matchAllQuery())
                .from(0)
                .size(5);
        final SearchRequest request = new SearchRequest(new String[]{IDX_TARGET}, query);

        StepVerifier.create(attributeDictionaryService.searchNoPredicateButLimited(5))
                .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals("CODE01"))
                .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals("CODE02"))
                .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals("CODE03"))
                .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals("CODE04"))
                .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals("CODE05"))
                .verifyComplete();
    }

    @Test
    void searchAsPageNumber0() {

        bulk();
        TestHelper.waitInSecond(1);

        final SearchSourceBuilder query = SearchSourceBuilder.searchSource()
                .query(QueryBuilders.matchAllQuery())
                .from(0)
                .size(5);
        final SearchRequest request = new SearchRequest(new String[]{IDX_TARGET}, query);

        var pageMono = attributeDictionaryService.searchAsPage(0, 5);

        StepVerifier.create(pageMono)
                .expectNextMatches(page -> page.content().size() == 5
                                            && page.pageMetadata().number() == 0)
                .verifyComplete();
    }
}
