package com.adeo.springboot.learning.sb3.controller;

import com.adeo.springboot.learning.sb3.dto.PageVideo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

@Testcontainers
@ContextConfiguration(initializers = VideoRestControllerTest.DataSourceInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VideoRestControllerTest {

    @Autowired TestRestTemplate restTemplate;

    @Container
    static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:15.5-alpine")
            .withUsername("michael")
            .withPassword("jordan")
            .withDatabaseName("videoDatabase");

    @BeforeEach
    void setup() {
        Assertions.assertThat(restTemplate).isNotNull();
    }

    @Test
    void all() {
        ResponseEntity<PageVideo> response = restTemplate.withBasicAuth("user", "${USER_PASSWORD}")
                .getForEntity("/videos", PageVideo.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        static final Logger log = LoggerFactory.getLogger(DataSourceInitializer.class);

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            List<String> datasource = new ArrayList<>();
            datasource.add(STR."spring.datasource.url=\{database.getJdbcUrl()}");
            datasource.add(STR."spring.datasource.username=\{database.getUsername()}");
            datasource.add(STR."spring.datasource.password=\{database.getPassword()}");
            log.info(" > datasource information {}.", datasource);

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext, datasource.toArray(String[]::new));
        }
    }
}
