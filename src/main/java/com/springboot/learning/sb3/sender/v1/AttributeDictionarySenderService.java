package com.springboot.learning.sb3.sender.v1;

import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnaryKey;
import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.exception.InvalidInputException;
import com.springboot.learning.sb3.mapper.v1.AttributeDictionaryAvroMapper;
import com.springboot.learning.sb3.sender.IAttributeDictionarySenderService;
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
public class AttributeDictionarySenderService implements IAttributeDictionarySenderService<AttributeDictionaryEntity> {

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
    @Override
    public Mono<AttributeDictionaryEntity> send(@NotNull AttributeDictionaryEntity entity) {

        log.info(" > Send by Stream-bridge, attribute is {}.", entity);

        if(Objects.isNull(entity.code()))
            throw new InvalidInputException("The code field in attribute dictionary is mandatory");

        return Mono.just(entity)
                .map(mapper::toAvro)
                .map(attributeDictionnary -> MessageBuilder.withPayload(attributeDictionnary)
                        .setHeader(KafkaHeaders.KEY, new AttributeDictionnaryKey(
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
