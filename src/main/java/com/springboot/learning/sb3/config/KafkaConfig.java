package com.springboot.learning.sb3.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfig {

    private PropertiesConfig.KafkaProducerConfiguration kafkaConfiguration;

    public KafkaConfig(PropertiesConfig.KafkaProducerConfiguration kafkaConfiguration) {
        this.kafkaConfiguration = kafkaConfiguration;
    }

    @Bean
    Properties kafkaProducerAvroSerializerProperties() {

        final var properties = new Properties();
        final var brokers = kafkaConfiguration.sslEndpointIdentificationAlgorithm().concat("://").concat(kafkaConfiguration.brokers());

        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

        properties.setProperty("sasl.jaas.config", kafkaConfiguration.saslJaasConfig());
        properties.setProperty("security.protocol", kafkaConfiguration.securityProtocol());
        properties.setProperty("sasl.mechanism", kafkaConfiguration.saslMechanism());
        properties.setProperty("schema.registry.url", kafkaConfiguration.schemaRegistryUrl());
        properties.setProperty("auto.register.schemas", kafkaConfiguration.schemaAutoRegister());
        properties.setProperty("basic.auth.credentials.source", kafkaConfiguration.basicAuthCredentialsSource());
        properties.setProperty("schema.registry.basic.auth.user.info",
                kafkaConfiguration.schemaRegistryBasicAuthUserInfo());

        return properties;
    }
}
