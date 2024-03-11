package com.springboot.learning.repository.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.common.JackHelper;
import com.springboot.learning.common.OpensearchHelper;
import com.springboot.learning.common.WaitHelper;
import com.springboot.learning.dictionary.domain.AttributeDictionaryEntity;
import com.springboot.learning.repository.impl.ReactiveOpensearchMappingRepository;
import com.springboot.learning.repository.impl.ReactiveOpensearchRepository;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.CountRequest;
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
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(OutputCaptureExtension.class)
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

    @Value("classpath:json/attribute01Up.json")
    Resource attribute01Up;

    @Value("classpath:json/attribute01DifferentlyWrittenCode.json")
    Resource attribute01DifferentlyWrittenCode;

    @Value("classpath:json/attribute02.json")
    Resource attribute02;

    @Value("classpath:json/attribute03.json")
    Resource attribute03;

    @Value("classpath:json/attribute03Up.json")
    Resource attribute03Up;

    @Value("classpath:json/attributes.json")
    Resource attributes;

    @Value("classpath:index/index_attribute_dictionary_v1.json")
    Resource indexAttributeDictionaryV1;

    private static final AtomicBoolean INDEX_IS_CREATED = new AtomicBoolean();
    private static final AtomicReference<String> IDX_TARGET = new AtomicReference<>();
    private static final Logger log = LoggerFactory.getLogger(AttributeRepositoryTest.class);

    @BeforeEach
    void setup() {
        opensearchRepository = new ReactiveOpensearchRepository(highLevelClient, jack);
        var opensearchMappingRepository = new ReactiveOpensearchMappingRepository(highLevelClient);

        synchronized (this) {
            if(!INDEX_IS_CREATED.get()) {
                opensearchMappingRepository.createIndex(
                                OpensearchHelper.INDEX_NAME_V1,
                                indexAttributeDictionaryV1,
                                true)
                        .block();
                INDEX_IS_CREATED.set(true);
                IDX_TARGET.set(OpensearchHelper.INDEX_NAME_V1);
            }
        }
    }

    @AfterEach
    void after() {
        WaitHelper.waitInSecond(1);
        var deletedNumber = opensearchRepository.deleteAll(IDX_TARGET.get()).block();
        log.info(" > Delete all elements {}.", deletedNumber);
        WaitHelper.waitInSecond(1);
    }

    @Test
    void getById() throws IOException {

        final var entity = JackHelper.getAttributeCandidate(jack, attribute01, AttributeDictionaryEntity.class);

        StepVerifier.create(opensearchRepository.save(IDX_TARGET.get(),
                                entity, Optional.of(entity.code()),
                                AttributeDictionaryEntity.class))
            .expectNextMatches(attrEntity -> StringUtils.hasText(attrEntity.id())
                && attrEntity.id().equals(entity.code())
            )
            .verifyComplete();

        //Get by Identifier.
        StepVerifier.create(opensearchRepository.getById(IDX_TARGET.get(), entity.code(),
                                AttributeDictionaryEntity.class))
                .expectNextMatches(attrEntity -> attrEntity.id().equals(entity.code()))
                .verifyComplete();
    }

    @Test
    void bulk() {

        List<AttributeDictionaryEntity> entities = JackHelper.getManyAttributeCandidates(jack, attributes);

        final List<String> ids = entities.stream()
            .map(AttributeDictionaryEntity::code)
            .toList();

        Flux<ReactiveOpensearchRepository.CrudResult> entityFlux = opensearchRepository.bulk(entities,
            Optional.of(ids),
            IDX_TARGET.get());

        StepVerifier.create(entityFlux)
            .expectNextMatches(crudResult -> crudResult.status() == 201 && crudResult.id().equals(ids.getFirst()))
            .expectNextMatches(crudResult -> crudResult.status() == 201 && crudResult.id().equals(ids.get(1)))
            .expectNextMatches(crudResult -> crudResult.status() == 201 && crudResult.id().equals(ids.get(2)))
            .expectNextMatches(crudResult -> crudResult.status() == 201 && crudResult.id().equals(ids.get(3)))
            .expectNextMatches(crudResult -> crudResult.status() == 201 && crudResult.id().equals(ids.get(4)))
            .verifyComplete();
    }

    @Test
    void deleteOne() throws IOException {

        final String id = saveAnAttribute();

        //Delete it.
        StepVerifier.create(opensearchRepository.delete(IDX_TARGET.get(), id))
            .expectNextMatches(status -> status.equals(200))
            .verifyComplete();
    }

    @Test
    void deleteIn() {

        bulk();
        WaitHelper.waitInSecond(1);

        SearchSourceBuilder query = SearchSourceBuilder.searchSource()
            .query(QueryBuilders.matchAllQuery())
            .from(0)
            .size(10);

        var request = new SearchRequest(new String[]{IDX_TARGET.get()}, query);

        List<String> attributeIds = opensearchRepository.search(request, AttributeDictionaryEntity.class)
            .toStream()
            .map(AttributeDictionaryEntity::id)
            .sorted()
            .limit(5)
            .toList();

        StepVerifier.create(opensearchRepository.deleteIn(IDX_TARGET.get(), attributeIds).flatMapIterable(list -> list))
            .expectNextMatches(crudResult -> crudResult.status() == 200 && crudResult.id().equals(attributeIds.getFirst()))
            .expectNextMatches(crudResult -> crudResult.status() == 200 && crudResult.id().equals(attributeIds.get(1)))
            .expectNextMatches(crudResult -> crudResult.status() == 200 && crudResult.id().equals(attributeIds.get(2)))
            .expectNextMatches(crudResult -> crudResult.status() == 200 && crudResult.id().equals(attributeIds.get(3)))
            .expectNextMatches(crudResult -> crudResult.status() == 200 && crudResult.id().equals(attributeIds.get(4)))
            .verifyComplete();
    }

    @Tag("Save an attribute with auto identifier")
    @Test
    void saveWithAutoIdentifier(CapturedOutput output) throws IOException {

        final var entity = JackHelper.getAttributeCandidate(jack, attribute01, AttributeDictionaryEntity.class);

        var entityMono = opensearchRepository.save(IDX_TARGET.get(),
            entity, Optional.empty(),
            AttributeDictionaryEntity.class);

        StepVerifier.create(entityMono)
            .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals(entity.code())
                && Objects.nonNull(attributeDictionaryEntity.id())
                && !attributeDictionaryEntity.id().equals(entity.code())
            )
            .verifyComplete();

        Assertions.assertThat(output.getOut()).doesNotContain(" > The id of entity created is CODE01.");
    }

    @Tag("Save an attribute with manual identifier")
    @Test
    void saveWithManualIdentifier(CapturedOutput output) throws IOException {

        final var entity = JackHelper.getAttributeCandidate(jack, attribute01, AttributeDictionaryEntity.class);

        var entityMono = opensearchRepository.save(IDX_TARGET.get(),
            entity, Optional.of(entity.code()),
            AttributeDictionaryEntity.class);

        StepVerifier.create(entityMono)
            .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals(entity.code())
                && attributeDictionaryEntity.id().equals(entity.code())
            )
            .verifyComplete();

        Assertions.assertThat(output.getOut()).contains(" > The id of entity created is CODE01.");
    }

    @Test
    void update() throws IOException {

        final String id = saveAnAttribute();

        final var attributeCandidate = JackHelper.getAttributeCandidate(jack, attribute01Up,
            AttributeDictionaryEntity.class);

        //Launch the update
        Mono<AttributeDictionaryEntity> attributeUpdated = opensearchRepository.update(IDX_TARGET.get(),
                id,
                attributeCandidate,
                AttributeDictionaryEntity.class);

        StepVerifier.create(attributeUpdated)
            .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.referenceDataName().equals("REF_NAME_0100"))
            .verifyComplete();
    }

    @Tag("Upsert by search and execute an update")
    @Test
    void upsertBySearchAsUpdate(CapturedOutput output) throws IOException {

        saveAnAttribute();
        WaitHelper.waitInSecond(1);

        final var attributeCandidate = JackHelper.getAttributeCandidate(jack, attribute01Up,
            AttributeDictionaryEntity.class);

        var fieldName = "code";
        var fieldValue = attributeCandidate.code();

        StepVerifier.create(opensearchRepository.upsert(IDX_TARGET.get(),
                fieldName, fieldValue,
                attributeCandidate, AttributeDictionaryEntity.class))
            .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals(fieldValue)
                && attributeDictionaryEntity.referenceDataName().equals("REF_NAME_0100")
                && attributeDictionaryEntity.type().equals("TYPE0100")
            )
            .verifyComplete();

        Assertions.assertThat(output.getOut()).contains(" > The entity already exist, an update will be made.");
        Assertions.assertThat(output.getOut()).contains(" > The entity of type=AttributeDictionaryEntity has been updated.");
    }

    @Tag("Upsert by identifier and execute an update")
    @Test
    void upsertByIdAsUpdate(CapturedOutput output) throws IOException {

        final String id = saveAnAttribute();

        final var attributeCandidate = JackHelper.getAttributeCandidate(jack, attribute01Up,
            AttributeDictionaryEntity.class);

        StepVerifier.create(opensearchRepository.upsert(IDX_TARGET.get(),
                        attributeCandidate,
                        id,
                        AttributeDictionaryEntity.class))
            .expectNextMatches(attributeDictionaryEntity ->
                attributeDictionaryEntity.code().equals(attributeCandidate.code())
                && attributeDictionaryEntity.id().equals(attributeCandidate.code())
                && attributeDictionaryEntity.referenceDataName().equals("REF_NAME_0100")
                && attributeDictionaryEntity.type().equals("TYPE0100")
            )
            .verifyComplete();

        Assertions.assertThat(output.getOut()).contains(" > The entity already exist, an update will be made.");
        Assertions.assertThat(output.getOut()).contains(" > The entity of type=AttributeDictionaryEntity has been updated.");
    }

    @Tag("Upsert by identifier and execute a save on empty index")
    @Test
    void upsertByIdAsSaveEmptyIndex(@NotNull CapturedOutput output) throws IOException {

        final var attributeCandidate = JackHelper.getAttributeCandidate(jack, attribute01,
            AttributeDictionaryEntity.class);

        StepVerifier.create(opensearchRepository.upsert(IDX_TARGET.get(),
                                attributeCandidate,
                                attributeCandidate.code(),
                                AttributeDictionaryEntity.class))
            .expectNextMatches(attributeDictionaryEntity ->
                attributeDictionaryEntity.code().equals(attributeCandidate.code())
                && attributeDictionaryEntity.referenceDataName().equals("REF_NAME_01")
                && attributeDictionaryEntity.id().equals(attributeCandidate.code())
            )
            .verifyComplete();

        Assertions.assertThat(output.getOut()).contains(" > The id of entity created is " + attributeCandidate.code());
        Assertions.assertThat(output.getOut()).contains(" > The entity created has been find");
        Assertions.assertThat(output.getOut()).contains(" > The entity of type=AttributeDictionaryEntity has been saved.");
    }

    @Tag("Upsert by identifier and execute a save on index not exist")
    @Test
    void upsertByIdAsSaveIndexNotExist(CapturedOutput output) throws IOException {

        final var attributeCandidate = JackHelper.getAttributeCandidate(jack, attribute01,
            AttributeDictionaryEntity.class);

        StepVerifier.create(opensearchRepository.upsert(IDX_TARGET.get().concat("_unknown"),
                                attributeCandidate,
                                attributeCandidate.code(),
                                AttributeDictionaryEntity.class))
            .expectNextMatches(attributeDictionaryEntity ->
                attributeDictionaryEntity.code().equals(attributeCandidate.code())
                && attributeDictionaryEntity.referenceDataName().equals("REF_NAME_01")
                && attributeDictionaryEntity.id().equals(attributeCandidate.code())
            )
            .verifyComplete();

        Assertions.assertThat(output.getOut()).contains("no such index [attributes_dictionary_v1_unknown]");
        Assertions.assertThat(output.getOut()).contains(" > The id of entity created is " + attributeCandidate.code());
        Assertions.assertThat(output.getOut()).contains(" > The entity created has been find");
        Assertions.assertThat(output.getOut()).contains(" > The entity of type=AttributeDictionaryEntity has been saved.");
    }

    @Tag("Upsert by identifier and save/update an attribute more completed")
    @Test
    void upsertByIdAsSaveAttributeMoreCompleted(CapturedOutput output) throws IOException {

        //A save will be executed
        final var attributeCandidate = JackHelper.getAttributeCandidate(jack, attribute03,
            AttributeDictionaryEntity.class);

        StepVerifier.create(opensearchRepository.upsert(IDX_TARGET.get(),
                                attributeCandidate,
                                attributeCandidate.code(),
                                AttributeDictionaryEntity.class))
            .expectNextMatches(attribute ->
                attribute.code().equals(attributeCandidate.code())
                && attribute.id().equals(attributeCandidate.code())
                && attribute.referenceDataName().equals("REF_NAME_01")
                && attribute.guidelines().equals(List.of("check_before", "check_after"))
                && attribute.availableLocales().equals(List.of("fr", "en", "gr", "it"))
                && attribute.allowedExtensions().equals(List.of("Ext01", "Ext02"))
            )
            .verifyComplete();

        Assertions.assertThat(output.getOut()).contains(" > The id of entity created is " + attributeCandidate.code());
        Assertions.assertThat(output.getOut()).contains(" > The entity created has been find");
        Assertions.assertThat(output.getOut()).contains(" > The entity of type=AttributeDictionaryEntity has been saved.");

        //An update will be performed
        final var attributeCandidateUp = JackHelper.getAttributeCandidate(jack, attribute03Up,
                AttributeDictionaryEntity.class);

        StepVerifier.create(opensearchRepository.upsert(IDX_TARGET.get(),
                        attributeCandidateUp,
                        attributeCandidateUp.code(),
                        AttributeDictionaryEntity.class))
                .expectNextMatches(attribute ->
                        attribute.code().equals(attributeCandidateUp.code())
                        && attribute.id().equals(attributeCandidateUp.code())
                        && attribute.referenceDataName().equals("REF_NAME_01")
                        && attribute.guidelines().equals(List.of("check_before", "check_after"))
                        && attribute.availableLocales().equals(List.of("fr", "en", "gr", "it", "es"))
                        && attribute.allowedExtensions().equals(List.of("Ext01", "Ext02", "Ext03"))
                )
                .verifyComplete();

        Assertions.assertThat(output.getOut()).contains(" > The entity already exist, an update will be made.");
        Assertions.assertThat(output.getOut()).contains(" > The entity of type=AttributeDictionaryEntity has been updated.");
    }

    @Tag("Simple count")
    @Test
    void count() {

        bulk();
        WaitHelper.waitInSecond(1);

        Mono<Long> resultMono = opensearchRepository.count(new CountRequest(IDX_TARGET.get()));
        StepVerifier.create(resultMono)
            .expectNext(5L)
            .verifyComplete();
    }

    @Tag("Simple search by match all query")
    @Test
    void search() {

        bulk();
        WaitHelper.waitInSecond(1);

        final SearchSourceBuilder query = SearchSourceBuilder.searchSource()
            .query(QueryBuilders.matchAllQuery())
            .from(0)
            .size(5);
        final SearchRequest request = new SearchRequest(new String[]{IDX_TARGET.get()}, query);

        StepVerifier.create(opensearchRepository.search(request, AttributeDictionaryEntity.class))
            .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals("CODE01"))
            .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals("CODE02"))
            .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals("CODE03"))
            .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals("CODE04"))
            .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals("CODE05"))
            .verifyComplete();
    }

    @Tag("Manage the pagination : page 0 and size 5")
    @Test
    void searchAsPageNumber0AndSize5() {

        bulk();
        WaitHelper.waitInSecond(1);

        final SearchSourceBuilder query = SearchSourceBuilder.searchSource()
            .query(QueryBuilders.matchAllQuery())
            .from(0)
            .size(5);
        final SearchRequest request = new SearchRequest(new String[]{IDX_TARGET.get()}, query);

        var pageMono = opensearchRepository.searchAsPage(request,
            new CountRequest(IDX_TARGET.get()),
            AttributeDictionaryEntity.class);

        StepVerifier.create(pageMono)
            .expectNextMatches(page -> page.content().size() == 5
                && page.pageMetadata().number() == 0)
            .verifyComplete();
    }

    /**
     * @return @{@link String}
     */
    String saveAnAttribute() throws IOException {

        //Prepare an attribute
        final AttributeDictionaryEntity entity = jack.readValue(attribute01.getInputStream(), AttributeDictionaryEntity.class);
        Assertions.assertThat(entity).isNotNull();

        var entityMono = opensearchRepository.save(IDX_TARGET.get(), entity, Optional.of(entity.code()), AttributeDictionaryEntity.class);
        StepVerifier.create(entityMono)
                .expectNextMatches(attributeDictionaryEntity -> attributeDictionaryEntity.code().equals(entity.code())
                    && attributeDictionaryEntity.id().equals(entity.code())
                )
                .verifyComplete();

        return entity.code();
    }
}
