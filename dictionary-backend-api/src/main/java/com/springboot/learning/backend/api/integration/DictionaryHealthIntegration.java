package com.springboot.learning.backend.api.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class DictionaryHealthIntegration {

    private final WebClient dictionaryWebClient;

    private static final Logger log = LoggerFactory.getLogger(DictionaryHealthIntegration.class);

    public DictionaryHealthIntegration(WebClient dictionaryWebClient) {
        this.dictionaryWebClient = dictionaryWebClient;
    }

    /**
     * @return flow of {@link Health}
     */
    public Mono<Health> getDictionaryHealth() {

        return dictionaryWebClient.get()
            .uri(uriBuilder -> uriBuilder.pathSegment("management", "health").build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String.class)
            .doOnError(t -> log.error(t.getMessage(), t))
            .map(s -> new Health.Builder().up().build())
            .onErrorResume(t -> Mono.just(new Health.Builder().down().build()));
    }
}
