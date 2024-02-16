package com.springboot.learning.sync.tests;

import com.adeo.bonsai.dictionary.attribute.synchronisation.event.AttributeDictionary;
import com.adeo.bonsai.dictionary.attribute.synchronisation.event.AttributeDictionaryKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.repository.domain.AttributeDictionaryEntity;
import com.springboot.learning.sync.mapper.AttributeDictionaryAvroMapper;
import com.springboot.learning.sync.tests.helper.TestHelper;
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
import org.springframework.cloud.stream.binder.test.OutputDestination;
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
    OutputDestination outputDestination;

    @Autowired
    ObjectMapper jack;

    @Value("classpath:json/attribute01.json")
    Resource attribute01;

    static final AtomicBoolean INDEX_IS_CREATED = new AtomicBoolean();
    static final AttributeDictionaryAvroMapper mapper = Mappers.getMapper(AttributeDictionaryAvroMapper.class);

    @BeforeEach
    void setup() {

        synchronized (this) {
            if(!INDEX_IS_CREATED.get()) {
                var result = TestHelper.putIndexV1(opensearch.getHttpHostAddress());
                result.ifPresent(openSearchIndexCreationResult -> INDEX_IS_CREATED.set(openSearchIndexCreationResult.acknowledged()));
            }
        }
    }

    @Test
    void send(CapturedOutput output) throws IOException {

        final var entity = TestHelper.getAttributeCandidate(jack, attribute01, AttributeDictionaryEntity.class);
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
