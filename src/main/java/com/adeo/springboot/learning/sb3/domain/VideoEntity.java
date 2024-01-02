package com.adeo.springboot.learning.sb3.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table("video")
public record VideoEntity(@Id Long id, String name, String description, String username) {

    public VideoEntity {
        Objects.requireNonNull(name);
        Objects.requireNonNull(username);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoEntity video = (VideoEntity) o;
        return Objects.equals(name, video.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
