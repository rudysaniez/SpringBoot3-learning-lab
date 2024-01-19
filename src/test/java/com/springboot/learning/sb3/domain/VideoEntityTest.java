package com.springboot.learning.sb3.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class VideoEntityTest {

    @Test
    void newVideoMustHaveAnIdIsNull() {

        var video = new VideoEntity(null, "Learn Spring boot 3",
                "The powerful framework",
                "user");

        Assertions.assertThat(video.getId()).isNull();
        Assertions.assertThat(video.getVideoName()).isEqualTo("Learn Spring boot 3");
        Assertions.assertThat(video.getDescription()).isEqualTo("The powerful framework");
        Assertions.assertThat(video.getUsername()).isEqualTo("user");
    }

    @Test
    void newVideoThrowIllegalArgumentException() {

        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class,
                () -> new VideoEntity(null, null, "The powerful framework", "user"));
    }
}
