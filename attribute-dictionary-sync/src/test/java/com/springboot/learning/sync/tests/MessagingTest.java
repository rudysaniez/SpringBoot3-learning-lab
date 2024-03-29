package com.springboot.learning.sync.tests;

import com.adeo.bonsai.dictionary.attribute.synchronisation.event.AttributeDictionary;
import com.adeo.bonsai.dictionary.attribute.synchronisation.event.AttributeDictionaryKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.common.JackHelper;
import com.springboot.learning.common.OpensearchHelper;
import com.springboot.learning.dictionary.domain.AttributeDictionaryEntity;
import com.springboot.learning.repository.impl.ReactiveOpensearchMappingRepository;
import com.springboot.learning.sync.mapper.AttributeDictionaryAvroMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@ExtendWith(OutputCaptureExtension.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true"})
@Import({TestChannelBinderConfiguration.class})
@Tag("attribute-messages-test")
class MessagingTest {

    @Container
    static final OpensearchContainer<?> opensearch = new OpensearchContainer<>("opensearchproject/opensearch:2.11.1")
            .withStartupAttempts(5)
            .withStartupTimeout(Duration.ofMinutes(2));

    @DynamicPropertySource
    static void dynProps(DynamicPropertyRegistry registry) {
        registry.add("opensearch.uris", opensearch::getHttpHostAddress);
    }

    @Autowired
    Consumer<Message<AttributeDictionary>> attributeDictionarySyncEventConsume;

    @Autowired
    ObjectMapper jack;

    @Autowired
    ReactiveOpensearchMappingRepository mappingRepository;

    @Value("classpath:json/attribute01.json")
    Resource attribute01;

    @Value("classpath:index/index_attribute_dictionary_v1.json")
    Resource indexAttributeDictionaryV1;

    static final AtomicBoolean INDEX_IS_CREATED = new AtomicBoolean();
    static final AttributeDictionaryAvroMapper mapper = Mappers.getMapper(AttributeDictionaryAvroMapper.class);

    @BeforeEach
    void setup() {

        synchronized (this) {
            if(!INDEX_IS_CREATED.get()) {
                mappingRepository.createIndex(
                    OpensearchHelper.INDEX_NAME_V1,
                    indexAttributeDictionaryV1,
                    true)
                .block();
                INDEX_IS_CREATED.set(true);
            }
        }
    }

    @Test
    void send(CapturedOutput output) throws IOException {

        final var entity = JackHelper.getAttributeCandidate(jack, attribute01, AttributeDictionaryEntity.class);
        final var avro = mapper.toAvro(entity);

        final var msg = MessageBuilder.withPayload(avro)
            .setHeader(KafkaHeaders.RECEIVED_KEY, new AttributeDictionaryKey(
                    ThreadLocalRandom.current().nextInt(20),
                    UUID.randomUUID().toString()))
            .build();

        attributeDictionarySyncEventConsume.accept(msg);

        Assertions.assertThat(output.getOut()).contains(" > [attributeDictionarySyncEventConsume function] Consume " +
                                                        "key=" + msg.getHeaders().get(KafkaHeaders.RECEIVED_KEY) + " and " +
                                                        "body=" + msg.getPayload());
        Assertions.assertThat(output.getOut()).contains(" > This attribute is saved");
        Assertions.assertThat(output.getOut()).contains(" > The save asynchronously is complete.");
    }
}
