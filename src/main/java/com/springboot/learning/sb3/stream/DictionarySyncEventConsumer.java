package com.springboot.learning.sb3.stream;

import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnary;
import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnaryKey;
import com.springboot.learning.sb3.mapper.AttributeAvroMapper;
import com.springboot.learning.sb3.service.v1.AttributeDictionaryService;
import org.awaitility.Awaitility;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Configuration
public class DictionarySyncEventConsumer {

    private final AttributeDictionaryService attributeDictionaryService;

    private static final AttributeAvroMapper mapper = Mappers.getMapper(AttributeAvroMapper.class);
    private static final Logger log = LoggerFactory.getLogger(DictionarySyncEventConsumer.class);

    public DictionarySyncEventConsumer(AttributeDictionaryService attributeDictionaryService) {
        this.attributeDictionaryService = attributeDictionaryService;
    }

    @Bean
    Consumer<Message<AttributeDictionnary>> attributeDictionarySyncEventConsume() {

        return event -> {

            final AttributeDictionnaryKey key = (AttributeDictionnaryKey)event.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            final AttributeDictionnary payload = event.getPayload();
            log.info(" > [Kafka-By-Spring-Cloud-Stream] Consume key={} and body={}", key, payload);

            final var disposable = Mono.just(payload)
                    .map(mapper::toModel)
                    .flatMap(attributeDictionaryService::save)
                    .subscribe(attributeDictionaryEntity -> log.info(" > This attribute is saved {}", attributeDictionaryEntity));

            Awaitility.await().until(disposable::isDisposed);
        };
    }
}
