package com.adeo.springboot.learning.sb3.dto;

import java.util.Objects;
import java.util.Optional;

public record VideoSearch(String name, Optional<String> description) {

    public VideoSearch {
        Objects.requireNonNull(name);
        Objects.requireNonNull(description);
    }
}
