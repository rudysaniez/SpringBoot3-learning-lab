package com.springboot.learning.eureka.server.tests;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EurekaServerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Value("${security.eureka-username}")
    private String username;

    @Value("${security.eureka-password}")
    private String password;

    @Test
    void catalogLoads() {

        String expectedReponseBody = "{\"applications\":{\"versions__delta\":\"1\",\"apps__hashcode\":\"\",\"application\":[]}}";
        ResponseEntity<String> entity = testRestTemplate.withBasicAuth(username, password)
            .getForEntity("/eureka/apps", String.class);
        Assertions.assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(entity.getBody()).contains(expectedReponseBody);
    }

    @Test
    void healthy() {
        String partialResponseBody = "{\"status\":\"UP\"";
        ResponseEntity<String> entity = testRestTemplate.withBasicAuth(username, password)
            .getForEntity("/management/health", String.class);
        Assertions.assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(entity.getBody()).contains(partialResponseBody);
    }
}
