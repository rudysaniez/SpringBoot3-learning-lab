package com.springboot.learning.sb3.dto;

import java.util.Objects;

public record Video(String videoName, String description, String username) {

    public Video {
        Objects.requireNonNull(videoName);
    }
}
