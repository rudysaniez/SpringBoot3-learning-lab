package com.springboot.learning.backend.api.integration.exception;

public class MicroserviceCalledException extends RuntimeException {
    public MicroserviceCalledException(String message) {
        super(message);
    }
}
