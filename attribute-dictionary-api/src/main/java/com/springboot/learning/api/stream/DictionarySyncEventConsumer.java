package com.springboot.learning.api.stream;

import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnary;
import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnaryKey;
import com.springboot.learning.api.mapper.AttributeDictionaryAvroMapper;
import com.springboot.learning.service.contract.v1.impl.AttributeDictionaryService;
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

    private static final AttributeDictionaryAvroMapper mapper = Mappers.getMapper(AttributeDictionaryAvroMapper.class);
    private static final Logger log = LoggerFactory.getLogger(DictionarySyncEventConsumer.class);

    public DictionarySyncEventConsumer(AttributeDictionaryService attributeDictionaryService) {
        this.attributeDictionaryService = attributeDictionaryService;
    }

    @Bean
    Consumer<Message<AttributeDictionnary>> attributeDictionarySyncEventConsume() {

        return event -> {

            final AttributeDictionnaryKey key = (AttributeDictionnaryKey)event.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            final AttributeDictionnary payload = event.getPayload();
            log.info(" > [attributeDictionarySyncEventConsume function] Consume key={} and body={}", key, payload);

            final var disposable = Mono.just(payload)
                    .map(mapper::toEntity)
                    .flatMap(attributeDictionaryService::save)
                    .subscribe(attributeDictionaryEntity -> log.info(" > This attribute is saved {}", attributeDictionaryEntity),
                            throwable -> log.error(throwable.getMessage(), throwable),
                            () -> log.debug(" > The save asynchronously is complete.")
                    );

            Awaitility.await().until(disposable::isDisposed);
        };
    }
}
