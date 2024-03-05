package com.springboot.learning.backend.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class WebClientBackendConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientBackendConfig.class);

    /**
     * @return {@link ExchangeStrategies}
     * @since 1.2.1
     */
    @Bean
    ExchangeStrategies exchangeStrategies() {
        final int size = 2 * 1024 * 1024;

        return ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();
    }

    @LoadBalanced
    @Bean
    WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Profile("test")
    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * @param microservices : the microservices information
     * @return {@link WebClient}
     */
    @Bean
    WebClient dictionaryWebClient(WebClient.Builder webClientBuilder,
                                  PropertiesBackendConfig.Microservices microservices) {

        log.info(" > The dictionaryWebClient is created with uri={}", microservices.dictionaryApiUri());

        return webClientBuilder.defaultHeaders(headers ->
                    headers.setBasicAuth(
                        microservices.dictionaryBasicAuthUsername(),
                        microservices.dictionaryBasicAuthPassword())
                )
                .exchangeStrategies(exchangeStrategies())
                .uriBuilderFactory(new DefaultUriBuilderFactory(microservices.dictionaryApiUri()))
                .build();
    }
}
