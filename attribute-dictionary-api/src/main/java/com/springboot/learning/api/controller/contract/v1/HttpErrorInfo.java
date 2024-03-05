package com.springboot.learning.api.controller.contract.v1;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public record HttpErrorInfo(String path, HttpStatus httpStatus, String message, LocalDateTime timestamp) {

    public HttpErrorInfo {
        Objects.requireNonNull(path);
        Objects.requireNonNull(httpStatus);
        Objects.requireNonNull(message);
        Objects.requireNonNull(timestamp);
    }
}
