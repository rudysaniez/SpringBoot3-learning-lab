package com.springboot.learning.sb3.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.time.Duration;

public class TestHelper {

    public static <T> T getAttributeCandidate(ObjectMapper jack, Resource input, Class<T> type) throws IOException {
        return jack.readValue(input.getInputStream(), type);
    }

    public static void waitInSecond(int during) {
        try {
           Thread.sleep(Duration.ofSeconds(during));
        }
        catch(Exception e) {}
    }

    /**
     * @param webTestClient : the web test client
     * @param paths : the paths
     */
    public static void clean(WebTestClient webTestClient, String... paths) {
        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder.pathSegment(paths).build())
                .headers(header -> header.setBasicAuth("user", "user"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);
    }
}
