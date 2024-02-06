package com.springboot.learning.sb3.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.time.Duration;

public class TestHelper {

    private static final Logger log = LoggerFactory.getLogger(TestHelper.class);

    /**
     * @param jack : Jack !
     * @param input : the input resource
     * @param type : the type
     * @return {@link T}
     * @param <T> : the parameterized type
     * @throws IOException
     */
    public static <T> T getAttributeCandidate(ObjectMapper jack, Resource input, Class<T> type) throws IOException {
        return jack.readValue(input.getInputStream(), type);
    }

    /**
     * @param during : the duration
     */
    public static void waitInSecond(int during) {
        try {
           Thread.sleep(Duration.ofSeconds(during));
        }
        catch(Exception e) {
            log.error(e.getMessage(), e);
        }
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
