package com.springboot.learning.backend.api.webclient.tests;

import com.adeo.pro.replenishment.api.dictionary.model.AttributeDictionary;
import com.adeo.pro.replenishment.api.dictionary.model.PageAttributeDictionary;
import com.springboot.learning.backend.api.MockServerConfigurationTest;
import com.springboot.learning.backend.api.mock.server.loader.AttributeMockServerLoader;
import com.springboot.learning.backend.api.mock.server.loader.DictionaryHealthMockServerLoader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class DictionaryWebClientTest extends MockServerConfigurationTest {

    @Autowired
    WebClient dictionaryWebClient;

    @MockBean
    ReactiveJwtDecoder jwtDecoder;

    @Test
    void getAttributeById() {

        var attributeById = dictionaryWebClient.get()
                .uri(uri -> uri.pathSegment("attributes", "isHomeDeliverable").build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AttributeDictionary.class)
                .doOnNext(System.out::println);

        StepVerifier.create(attributeById)
                .consumeNextWith(attribute -> {
                    Assertions.assertThat(attribute.getCode()).isEqualTo("isHomeDeliverable");
                })
                .verifyComplete();

        AttributeMockServerLoader.verifyRequest(getAttributeDictionaryMockServer(), AttributeMockServerLoader.ATTRIBUTE_PATH_BY_ID, 1);
    }

    @Test
    void getAttributeAsPage() {

        var attributeAsPage = dictionaryWebClient.get()
                .uri(uri -> uri.pathSegment("attributes").build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PageAttributeDictionary.class)
                .doOnNext(System.out::println);

        StepVerifier.create(attributeAsPage)
                .expectNextMatches(page -> page.getContent().getFirst().getCode().equals("isHomeDeliverable"))
                .verifyComplete();

        AttributeMockServerLoader.verifyRequest(getAttributeDictionaryMockServer(), AttributeMockServerLoader.ATTRIBUTE_PATH_AS_PAGE, 1);
    }

    @Test
    void health() {

        var health = dictionaryWebClient.get()
                .uri(uri -> uri.pathSegment("management", "health").build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(System.out::println);

        StepVerifier.create(health)
                .expectNextMatches(h -> h.contains("\"status\": \"UP\""))
                .verifyComplete();

        DictionaryHealthMockServerLoader.verifyRequest(getAttributeDictionaryMockServer(), DictionaryHealthMockServerLoader.HEALTH_PATH, 1);
    }
}
