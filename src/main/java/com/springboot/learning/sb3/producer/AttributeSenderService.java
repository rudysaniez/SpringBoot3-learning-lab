package com.springboot.learning.sb3.producer;

import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnaryKey;
import com.springboot.learning.sb3.config.PropertiesConfig;
import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.mapper.AttributeMapper;
import jakarta.validation.constraints.NotNull;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AttributeSenderService {

    private final StreamBridge streamBridge;
    private final ReactiveAttributeSenderService reactiveAttributeSenderService;
    private final PropertiesConfig.TopicConfiguration topicConfiguration;

    private final Executor taskExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private static final AttributeMapper mapper = Mappers.getMapper(AttributeMapper.class);

    private static final Logger log = LoggerFactory.getLogger(AttributeSenderService.class);

    public AttributeSenderService(StreamBridge streamBridge,
                                  ReactiveAttributeSenderService reactiveAttributeSenderService,
                                  PropertiesConfig.TopicConfiguration topicConfiguration) {
        this.streamBridge = streamBridge;
        this.reactiveAttributeSenderService = reactiveAttributeSenderService;
        this.topicConfiguration = topicConfiguration;
    }

    /**
     * @param entity : the entity
     * @return flow of {@link AttributeDictionaryEntity}
     */
    public Mono<AttributeDictionaryEntity> send(@NotNull AttributeDictionaryEntity entity) {

        log.info(" > Send this attribute={}", entity);

        return Mono.just(entity)
                .map(mapper::toAvro)
                .flatMap(attributeDictionnary -> reactiveAttributeSenderService.send(
                            new AttributeDictionnaryKey(ThreadLocalRandom.current().nextInt(20),
                                    UUID.randomUUID().toString()),
                            attributeDictionnary,
                            topicConfiguration.attributeTopic()
                        )
                )
                .thenReturn(entity);
    }

    /**
     * @param bindingName : the binding name
     * @param entity : the attribute entity
     * @return flow of {@link AttributeDictionaryEntity}
     */
    public Mono<AttributeDictionaryEntity> send(@NotNull String bindingName,
                                                @NotNull AttributeDictionaryEntity entity) {

        log.info(" > Send by Stream-bridge, attribute is {}", entity);

        return Mono.just(entity)
                .map(mapper::toAvro)
                .map(attributeDictionnary -> MessageBuilder.withPayload(attributeDictionnary)
                        .setHeader(KafkaHeaders.RECEIVED_KEY, new AttributeDictionnaryKey(
                                                                ThreadLocalRandom.current().nextInt(20),
                                                                UUID.randomUUID().toString()))
                        .build()
                )
                .doOnNext(message -> log.info(" > Message content {}", message))
                .flatMap(message -> Mono.fromCallable(() -> streamBridge.send(bindingName, message)))
                .thenReturn(entity)
                .subscribeOn(Schedulers.fromExecutor(taskExecutor));
    }
}
