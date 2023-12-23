package com.adeo.springboot.learning.sb3.dto;

import java.util.Objects;

public record Video(String name, String description) {

    public Video {
        Objects.requireNonNull(name);
    }
}
