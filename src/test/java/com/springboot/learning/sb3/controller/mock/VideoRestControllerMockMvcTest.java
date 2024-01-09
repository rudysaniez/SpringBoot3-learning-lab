package com.springboot.learning.sb3.controller.mock;

import com.springboot.learning.sb3.controller.VideoRestController;
import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.dto.Video;
import com.springboot.learning.sb3.mapper.VideoMapper;
import com.springboot.learning.sb3.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import reactor.core.publisher.Flux;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = VideoRestController.class)
class VideoRestControllerMockMvcTest {

    @Autowired MockMvc mvc;

    @MockBean VideoService videoService;
    @MockBean VideoMapper videoMapper;

    @BeforeEach
    void setup() {

        final var video1 = new VideoEntity(null, "Learn with Spring-boot 3", "Better framework", "user");
        final var video2 = new VideoEntity(null, "Learn Spring-data-jpa", "Java persistence API", "user");

        Mockito.when(videoService.findAll(0, 20))
                .thenReturn(Flux.fromIterable(List.of(video1, video2)));

        Mockito.when(videoMapper.toModel(video1))
                .thenReturn(new Video(video1.name(), video1.description(), video1.username()));

        Mockito.when(videoMapper.toModel(video2))
                .thenReturn(new Video(video2.name(), video2.description(), video2.username()));
    }

    @WithMockUser(username = "user")
    @Test
    void all() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/videos"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpectAll(
                        MockMvcResultMatchers.jsonPath("$.content[0].name").value("Learn with Spring-boot 3"),
                        MockMvcResultMatchers.jsonPath("$.content[0].username").value("user"),
                        MockMvcResultMatchers.jsonPath("$.content[1].name").value("Learn Spring-data-jpa"),
                        MockMvcResultMatchers.jsonPath("$.content[1].username").value("user"),

                        MockMvcResultMatchers.jsonPath("$.pageMetadata.pageNumber").value(0),
                        MockMvcResultMatchers.jsonPath("$.pageMetadata.pageSize").value(20),
                        MockMvcResultMatchers.jsonPath("$.pageMetadata.totalElements").value(2),
                        MockMvcResultMatchers.jsonPath("$.pageMetadata.totalPages").value(1)
                );
    }
}
