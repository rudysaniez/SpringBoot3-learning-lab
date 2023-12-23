package com.adeo.springboot.learning.sb3.domain;

import org.springframework.data.annotation.Id;

import java.util.Objects;

public record Video(@Id Long id, String name, String description) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return Objects.equals(name, video.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
