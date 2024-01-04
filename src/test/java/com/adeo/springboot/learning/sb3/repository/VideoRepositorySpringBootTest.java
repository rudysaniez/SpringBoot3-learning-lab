package com.adeo.springboot.learning.sb3.repository;

import com.adeo.springboot.learning.sb3.domain.VideoEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.StreamSupport;

@SpringBootTest
class VideoRepositorySpringBootTest {

    @Autowired VideoRepository videoRepository;
    @Autowired VideoListRepository videoListRepository;

    @Test
    void findAll() {

        List<VideoEntity> entityList = StreamSupport.stream(videoRepository.findAll().spliterator(), false)
                .toList();
        Assertions.assertThat(entityList).hasSize(3);

        entityList = videoListRepository.findAll();
        Assertions.assertThat(entityList).hasSize(3);
    }
}
