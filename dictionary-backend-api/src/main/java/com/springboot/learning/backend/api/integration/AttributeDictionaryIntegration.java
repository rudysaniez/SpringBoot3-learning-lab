package com.springboot.learning.backend.api.integration;

import com.adeo.pro.replenishment.api.dictionary.model.AttributeDictionary;
import com.adeo.pro.replenishment.api.dictionary.model.AttributesApi;
import com.adeo.pro.replenishment.api.dictionary.model.PageAttributeDictionary;
import com.adeo.pro.replenishment.api.dictionary.model.PageMetadata;
import com.springboot.learning.backend.api.controller.contract.v1.AttributeDictionaryModel;
import com.springboot.learning.backend.api.controller.contract.v1.PageMetadataModel;
import com.springboot.learning.backend.api.controller.contract.v1.PageModel;
import com.springboot.learning.backend.api.integration.exception.MicroserviceCalledException;
import com.springboot.learning.backend.api.integration.retry.RetryStrategy;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AttributeDictionaryIntegration implements AttributesApi {

    private final WebClient dictionaryWebClient;
    private final RetryStrategy retryStrategy;

    private static final Logger log = LoggerFactory.getLogger(AttributeDictionaryIntegration.class);

    public AttributeDictionaryIntegration(WebClient dictionaryWebClient,
                                          RetryStrategy retryStrategy) {
        this.dictionaryWebClient = dictionaryWebClient;
        this.retryStrategy = retryStrategy;
    }

    @Override
    public Mono<ResponseEntity<Long>> deleteAllAttributes(ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteOneAttribute(String id, ServerWebExchange exchange) {
        return null;
    }

    /**
     * @param page  (optional, default to 0)
     * @param size  (optional, default to 5)
     * @param exchange : the server web exchange
     * @return {@link PageAttributeDictionary}
     */
    @TimeLimiter(name = "attributes")
    @CircuitBreaker(name = "attributes", fallbackMethod = "getAllAttributesFallback")
    @Override
    public Mono<ResponseEntity<PageAttributeDictionary>> getAllAttributes(Integer page,
                                                                          Integer size,
                                                                          ServerWebExchange exchange) {

        return dictionaryWebClient.get()
            .uri(uriBuilder -> uriBuilder.pathSegment("attributes")
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::is5xxServerError,
                clientResponse -> Mono.error(new MicroserviceCalledException("Get a page of attributes failed")))
            .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatusCode.valueOf(HttpStatus.NO_CONTENT.value())),
                clientResponse -> Mono.empty())
            .bodyToMono(PageAttributeDictionary.class)
            .doOnError(t -> log.error(t.getMessage(), t))
            //.retryWhen(retryStrategy.retryBackoff())
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * @param page
     * @param size
     * @param exchange
     * @param ex
     * @return
     */
    public Mono<ResponseEntity<PageAttributeDictionary>> getAllAttributesFallback(Integer page,
                                                                                  Integer size,
                                                                                  ServerWebExchange exchange,
                                                                                  CallNotPermittedException ex) {

        log.warn("Creating a fail-fast fallback attributes for page = {}, size = {} and exception = {}.",
                page, size, ex.toString());

        final var empty = new PageAttributeDictionary()
            .content(List.of())
            .pageMetadata(new PageMetadata().number(0).size(0).totalElements(0L).totalPages(0L));

        return Mono.just(empty)
            .map(ResponseEntity::ok)
            ;
    }

    /**
     * @param id  (required)
     * @param exchange : the server web exchange
     * @return {@link AttributeDictionary}
     */
    @Override
    public Mono<ResponseEntity<AttributeDictionary>> getAttributeById(String id, ServerWebExchange exchange) {

        return dictionaryWebClient.get()
            .uri(uriBuilder -> uriBuilder.pathSegment("attributes", id).build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::is5xxServerError,
                clientResponse -> Mono.error(new MicroserviceCalledException("Get an attribute by id=" + id + " failed")))
            .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatusCode.valueOf(HttpStatus.NO_CONTENT.value())),
                clientResponse -> Mono.empty())
            .bodyToMono(AttributeDictionary.class)
            .doOnError(t -> log.error(t.getMessage(), t))
            .retryWhen(retryStrategy.retryBackoff())
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<AttributeDictionary>> saveAttribute(Mono<AttributeDictionary> attributeDictionary, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Void>> saveAttributeAsync(Mono<AttributeDictionary> attributeDictionary, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Flux<AttributeDictionary>>> searchAttributes(String q, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<AttributeDictionary>> updateAttribute(String id, Mono<AttributeDictionary> attributeDictionary, ServerWebExchange exchange) {
        return null;
    }
}
