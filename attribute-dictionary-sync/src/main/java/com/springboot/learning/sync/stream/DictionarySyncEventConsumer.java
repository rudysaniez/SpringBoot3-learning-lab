package com.springboot.learning.sync.stream;

import com.adeo.bonsai.dictionary.attribute.synchronisation.event.AttributeDictionary;
import com.adeo.bonsai.dictionary.attribute.synchronisation.event.AttributeDictionaryKey;
import com.springboot.learning.service.impl.AttributeDictionaryService;
import com.springboot.learning.sync.mapper.AttributeDictionaryAvroMapper;
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
    Consumer<Message<AttributeDictionary>> attributeDictionarySyncEventConsume() {

        return event -> {

            final AttributeDictionaryKey key = (AttributeDictionaryKey)event.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            final AttributeDictionary payload = event.getPayload();
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
