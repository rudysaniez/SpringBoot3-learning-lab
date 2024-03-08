package com.springboot.learning.api.config;

import com.springboot.learning.repository.impl.ReactiveOpensearchMappingRepository;
import com.springboot.learning.service.config.PropertiesServiceConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ApplicationRunnerDictionaryConfig {

    @Value("classpath:index/index_attribute_dictionary_v1.json")
    Resource indexAttributeDictionaryV1;

    @Bean
    ApplicationRunner opensearchAttributeIndexCreation(ReactiveOpensearchMappingRepository opensearchMappingRepository,
        @Value("classpath:index/index_attribute_dictionary_v1.json") Resource indexAttributeDictionaryV1,
        PropertiesServiceConfig.ApplicationOpensearchConfiguration indexConfig) {

        return args ->
            opensearchMappingRepository.createIndex(indexConfig.attributesDictionaryName(),
                            indexAttributeDictionaryV1,
                            false)
                .block();
    }
}
