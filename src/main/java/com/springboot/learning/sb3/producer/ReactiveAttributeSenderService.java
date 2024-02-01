package com.springboot.learning.sb3.producer;

import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnary;
import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnaryKey;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.util.Properties;

@Service
public class ReactiveAttributeSenderService {

    private final KafkaSender<AttributeDictionnaryKey, AttributeDictionnary> sender;

    public ReactiveAttributeSenderService(Properties kafkaProducerAvroSerializerProperties) {

        final SenderOptions<AttributeDictionnaryKey, AttributeDictionnary> senderOptions = SenderOptions
                .create(kafkaProducerAvroSerializerProperties);
        sender = KafkaSender.create(senderOptions);
    }

    /**
     * @param key : the attribute key
     * @param payload : the payload
     * @param topicName : the target topic name
     * @return {@link  AttributeDictionnary}
     */
    public Mono<AttributeDictionnary> send(AttributeDictionnaryKey key,
                                           AttributeDictionnary payload,
                                           String topicName) {

        final var fluxToSend = Flux.just(new AttrGlue(key, payload))
                .map(glue -> SenderRecord.create(new ProducerRecord<>(topicName, key, payload), key.getCorrelationId()));

        return sender.send(fluxToSend)
                .next()
                .thenReturn(payload);
    }

    record AttrGlue(AttributeDictionnaryKey key, AttributeDictionnary payload){}
}
