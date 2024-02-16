package com.springboot.learning.api.sender.service;

import com.adeo.bonsai.dictionary.attribute.synchronisation.event.AttributeDictionaryKey;
import com.springboot.learning.api.mapper.AttributeDictionaryAvroMapper;
import com.springboot.learning.repository.domain.AttributeDictionaryEntity;
import com.springboot.learning.service.exception.InvalidInputException;
import jakarta.validation.constraints.NotNull;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

@ConditionalOnProperty(prefix = "service", name = "version", havingValue = "v1")
@Service
public class AttributeDictionarySenderService {

    private final StreamBridge streamBridge;
    private final Scheduler scheduler;

    private static final AttributeDictionaryAvroMapper mapper = Mappers.getMapper(AttributeDictionaryAvroMapper.class);

    public static final String BINDING_TARGET = "attributeDictionarySyncEventConsume-out-0";
    private static final Logger log = LoggerFactory.getLogger(AttributeDictionarySenderService.class);

    public AttributeDictionarySenderService(StreamBridge streamBridge, Executor taskExecutor) {
        this.streamBridge = streamBridge;
        this.scheduler = Schedulers.fromExecutor(taskExecutor);
    }

    /**
     * @param entity : the attribute entity
     * @return flow of {@link AttributeDictionaryEntity}
     */
    public Mono<AttributeDictionaryEntity> send(@NotNull AttributeDictionaryEntity entity) {

        log.info(" > Send by Stream-bridge, attribute is {}.", entity);

        if(Objects.isNull(entity.code()))
            throw new InvalidInputException("The code field in attribute dictionary is mandatory");

        return Mono.just(entity)
                .map(mapper::toAvro)
                .map(attributeDictionnary -> MessageBuilder.withPayload(attributeDictionnary)
                        .setHeader(KafkaHeaders.KEY, new AttributeDictionaryKey(
                                                                ThreadLocalRandom.current().nextInt(20),
                                                                UUID.randomUUID().toString()))
                        .build()
                )
                .doOnNext(message -> log.debug(" > Message content {}", message))
                .flatMap(message -> Mono.fromCallable(() -> streamBridge.send(BINDING_TARGET, message)))
                .doOnNext(result -> log.info(" > Message sent, result Y/N : {}.", result))
                .thenReturn(entity)
                .subscribeOn(scheduler);
    }
}
