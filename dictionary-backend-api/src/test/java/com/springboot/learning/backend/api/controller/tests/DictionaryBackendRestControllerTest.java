package com.springboot.learning.backend.api.controller.tests;

import com.springboot.learning.backend.api.MockServerConfigurationTest;
import com.springboot.learning.backend.api.controller.contract.v1.AttributeDictionaryModel;
import com.springboot.learning.backend.api.controller.contract.v1.PageModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class DictionaryBackendRestControllerTest extends MockServerConfigurationTest {

    @Autowired
    WebTestClient webClient;

    @MockBean
    ReactiveJwtDecoder jwtDecoder;

    @Test
    void getAttributeById() {

        Mono<AttributeDictionaryModel> attributeModelMono = webClient.get()
            .uri(uriBuilder -> uriBuilder.pathSegment("dictionary", "attributes", "isHomeDeliverable").build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .returnResult(AttributeDictionaryModel.class)
            .getResponseBody()
            .next();

        StepVerifier.create(attributeModelMono)
            .expectNextMatches(attribute -> attribute.code().equals("isHomeDeliverable"))
            .verifyComplete();
    }

    @Test
    void getAttributeAsPage() {

        Mono<PageModel<AttributeDictionaryModel>> attributePageModelMono = webClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("dictionary", "attributes").build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .returnResult(new ParameterizedTypeReference<PageModel<AttributeDictionaryModel>>() {})
                .getResponseBody()
                .next();

        StepVerifier.create(attributePageModelMono)
                .expectNextMatches(page -> page.content().getFirst().code().equals("isHomeDeliverable"))
                .verifyComplete();
    }

    @Test
    void getHealth() {

        Mono<String> healthMono = webClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("management", "health").build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .returnResult(String.class)
                .getResponseBody()
                .next();

        StepVerifier.create(healthMono.doOnNext(System.out::println))
                .expectNextMatches(healthAsString -> healthAsString.contains("{\"dictionary-api\":{\"status\":\"UP\"}}"))
                .verifyComplete();
    }
}
