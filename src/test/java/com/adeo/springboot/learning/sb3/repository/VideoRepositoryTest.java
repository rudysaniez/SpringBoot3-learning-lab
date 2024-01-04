package com.adeo.springboot.learning.sb3.repository;

import com.adeo.springboot.learning.sb3.domain.VideoEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@ContextConfiguration(initializers = VideoRepositoryTest.DataSourceInitializer.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DataJdbcTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class VideoRepositoryTest {

    @Autowired VideoRepository videoRepository;
    @Autowired VideoPagingRepository videoPagingRepository;
    @Autowired VideoListRepository videoListRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Container
    static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:15.5-alpine")
            .withUsername("michael")
            .withPassword("jordan")
            .withDatabaseName("videoDatabase");

    @BeforeEach
    void setup() {
        Assertions.assertThat(jdbcTemplate).isNotNull();
        Assertions.assertThat(videoRepository).isNotNull();
        Assertions.assertThat(videoPagingRepository).isNotNull();
        Assertions.assertThat(videoListRepository).isNotNull();
    }

    @Test
    void findAll() {

        List<VideoEntity> entityList = StreamSupport.stream(videoRepository.findAll().spliterator(), false)
                .toList();
        Assertions.assertThat(entityList).hasSize(3);

        entityList = videoListRepository.findAll();
        Assertions.assertThat(entityList).hasSize(3);
    }

    /**
     * <a href="https://www.baeldung.com/spring-tests-override-properties">Properties for the tests</a>
     */
    static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        static final Logger log = LoggerFactory.getLogger(DataSourceInitializer.class);

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            List<String> datasource = new ArrayList<>();
            datasource.add("spring.datasource.url=" + database.getJdbcUrl());
            log.info(" > datasource information {}.", datasource);

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext, datasource.toArray(String[]::new));
        }
    }
}
