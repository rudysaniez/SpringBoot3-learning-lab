package com.springboot.learning.sb3.stream;

import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnary;
import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnaryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class DictionarySyncEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DictionarySyncEventConsumer.class);

    @Bean
    Consumer<Message<AttributeDictionnary>> attributeDictionarySyncEventConsume() {

        return event -> {

            log.info(" > [Kafka-By-Spring-Cloud-Stream] Consume an attribute is {}", event.getPayload());
            final var key = (AttributeDictionnaryKey)event.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            final AttributeDictionnary body = event.getPayload();
            log.info(" > [Kafka-By-Spring-Cloud-Stream] Consume key={} and body={}", key, body);
        };
    }
}
