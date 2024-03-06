package com.springboot.learning.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Configuration
public class HealthCheckGatewayConfig {

    private final WebClient webClient;

    private static final Logger log = LoggerFactory.getLogger(HealthCheckGatewayConfig.class);

    public HealthCheckGatewayConfig(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder.build();
    }

    @Bean
    ReactiveHealthContributor coreServices(PropertiesGatewayConfig.Microservices microservices) {

        Map<String, ReactiveHealthIndicator> registry = new HashMap<>();
        registry.put("dictionary-api", () -> getHealth(microservices.dictionaryApiUri()));
        registry.put("backend-api", () -> getHealth(microservices.backendApiUri()));
        return CompositeReactiveHealthContributor.fromMap(registry);
    }

    /**
     *
     * @param baseUrl : the microservice base url
     * @return flow of {@link Health}
     */
    private Mono<Health> getHealth(String baseUrl) {
        String url = baseUrl + "/management/health";
        log.debug("Setting up a call to the Health API on URL: {}", url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log(log.getName(), Level.FINE);
    }
}
