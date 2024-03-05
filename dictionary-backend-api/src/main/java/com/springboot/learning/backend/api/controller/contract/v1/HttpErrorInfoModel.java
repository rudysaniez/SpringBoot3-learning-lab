package com.springboot.learning.backend.api.controller.contract.v1;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public record HttpErrorInfoModel(String path, HttpStatus httpStatus, String message, LocalDateTime timestamp) {

    public HttpErrorInfoModel {
        Objects.requireNonNull(path);
        Objects.requireNonNull(httpStatus);
        Objects.requireNonNull(message);
        Objects.requireNonNull(timestamp);
    }
}
