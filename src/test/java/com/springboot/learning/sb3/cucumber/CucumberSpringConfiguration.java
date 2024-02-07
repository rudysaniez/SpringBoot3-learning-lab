package com.springboot.learning.sb3.cucumber;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.spring.CucumberContextConfiguration;
import org.opensearch.testcontainers.OpensearchContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;
import java.util.Objects;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestChannelBinderConfiguration.class})
public class CucumberSpringConfiguration {

    static OpensearchContainer<?> opensearch;

    private static final Logger log = LoggerFactory.getLogger(CucumberSpringConfiguration.class);

    @BeforeAll
    static void beforeAll() {

        log.info(" > The opensearch container start");

        opensearch = new OpensearchContainer<>("opensearchproject/opensearch:2.11.1")
                .withStartupAttempts(5)
                .withStartupTimeout(Duration.ofMinutes(2));

        opensearch.start();

        log.info(" > The opensearch is started");
    }

    @DynamicPropertySource
    static void dynProps(DynamicPropertyRegistry registry) {

        if(Objects.isNull(opensearch)) {
            log.info(" > The opensearch container is null, the setup method is launched.");
            beforeAll();
        }

        log.info(" > The dynamic property registry is launched.");
        registry.add("opensearch.uris", opensearch::getHttpHostAddress);
        log.info(" > Information opensearch.uris={}.", opensearch.getHttpHostAddress());
    }

    @AfterAll
    public static void afterAll() {
        log.info(" > closing the opensearch.");
        opensearch.stop();
    }
}