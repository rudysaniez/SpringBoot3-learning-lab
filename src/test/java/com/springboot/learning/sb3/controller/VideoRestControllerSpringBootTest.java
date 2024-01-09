package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.dto.PageVideo;
import com.springboot.learning.sb3.dto.Video;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VideoRestControllerSpringBootTest {

    @Autowired TestRestTemplate restTemplate;

    @Test
    void all() {
        ResponseEntity<PageVideo> response = restTemplate.withBasicAuth("user", "user")
                .getForEntity("/videos", PageVideo.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(Objects.requireNonNull(response.getBody()).content().getFirst().name()).contains("test");
    }

    @Test
    void unauthorized() {
        ResponseEntity<PageVideo> response = restTemplate.getForEntity("/videos", PageVideo.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void forbidden() {

        ResponseEntity<Void> response = restTemplate.withBasicAuth("user_writer", "user_writer")
                .exchange("/videos?name=Learn Spring boot data jpa (test)", HttpMethod.DELETE, null, Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void insertAndDelete() {

        final String name = "Insert this video test";

        var httpEntity = new HttpEntity<>(new Video(name, "Simple example", "user_writer"));

        ResponseEntity<Video> response = restTemplate.withBasicAuth("user_writer", "user_writer")
                .exchange("/videos", HttpMethod.POST, httpEntity, Video.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Launch a search
        ResponseEntity<List<Video>> listResponseEntity = restTemplate.withBasicAuth("user_writer", "user_writer")
                .exchange("/videos/:search?name=" + name, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        Assertions.assertThat(listResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(listResponseEntity.getBody()).hasSize(1);

        //Delete
        ResponseEntity<Void> voidResponseEntity = restTemplate.withBasicAuth("user_writer", "user_writer")
                .exchange("/videos?name=" + name, HttpMethod.DELETE, null, Void.class);

        Assertions.assertThat(voidResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
