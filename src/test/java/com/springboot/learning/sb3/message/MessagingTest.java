package com.springboot.learning.sb3.message;

import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnary;
import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnaryKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.helper.TestHelper;
import com.springboot.learning.sb3.mapper.v1.AttributeDictionaryAvroMapper;
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
import org.springframework.cloud.stream.function.StreamBridge;
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
import java.util.function.Consumer;

@ExtendWith(OutputCaptureExtension.class)
@Testcontainers
@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
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
    Consumer<Message<AttributeDictionnary>> attributeDictionarySyncEventConsume;

    @Autowired
    OutputDestination outputDestination;

    @Autowired
    StreamBridge streamBridge;

    @Autowired
    ObjectMapper jack;

    @Value("classpath:json/attribute01.json")
    Resource attribute01;

    @Value("${spring.cloud.function.definition}")
    String functions;

    static final AttributeDictionaryAvroMapper mapper = Mappers.getMapper(AttributeDictionaryAvroMapper.class);

    @BeforeEach
    void setup() {
        Assertions.assertThat(functions).isEqualTo("attributeDictionarySyncEventConsume");
    }

    @Test
    void send(CapturedOutput output) throws IOException {

        final var entity = TestHelper.getAttributeCandidate(jack, attribute01, AttributeDictionaryEntity.class);
        final var attributeDictionaryAvro = mapper.toAvro(entity);

        final var msg = MessageBuilder.withPayload(attributeDictionaryAvro)
                .setHeader(KafkaHeaders.RECEIVED_KEY, new AttributeDictionnaryKey(
                        ThreadLocalRandom.current().nextInt(20),
                        UUID.randomUUID().toString()))
                .build();

        streamBridge.send("attributeDictionarySyncEventConsume-out-0", msg);

        TestHelper.waitInSecond(1);

        Assertions.assertThat(output.getOut()).contains(" > [attributeDictionarySyncEventConsume function] Consume");
        Assertions.assertThat(output.getOut()).contains(" > This attribute is saved ");
    }
}
