package com.springboot.learning.sb3.message;

import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.controller.contract.AttributeDictionary;
import com.springboot.learning.sb3.helper.TestHelper;
import com.springboot.learning.sb3.mapper.v1.AttributeDictionaryMapper;
import com.springboot.learning.sb3.producer.v1.AttributeDictionarySenderService;
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
import org.springframework.messaging.Message;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Duration;
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
    Consumer<Message<AttributeDictionnary>> attributeDictionarySyncEventConsume;

    @Autowired
    OutputDestination outputDestination;

    @Autowired
    AttributeDictionarySenderService attributeSenderService;

    @Autowired
    ObjectMapper jack;

    @Value("classpath:json/attribute01.json")
    Resource attribute01;

    @Value("${spring.cloud.function.definition}")
    String functions;

    static final AttributeDictionaryMapper mapper = Mappers.getMapper(AttributeDictionaryMapper.class);

    @BeforeEach
    void setup() {
        Assertions.assertThat(functions).isEqualTo("attributeDictionarySyncEventConsume");
    }

    @Test
    void send(CapturedOutput output) throws IOException {

        final var model = TestHelper.getAttributeCandidate(jack, attribute01, AttributeDictionary.class);
        final var entity = mapper.toEntity(model);

        attributeSenderService.send(entity).block();

        Assertions.assertThat(output.getOut()).contains(" > Send by Stream-bridge, attribute is " + entity.toString());
        Assertions.assertThat(output.getOut()).contains(" > Message sent, result Y/N : true.");
        Assertions.assertThat(output.getOut()).contains(" > [attributeDictionarySyncEventConsume function] Consume");
    }
}
