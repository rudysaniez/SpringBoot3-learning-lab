package com.springboot.learning.backend.api.controller.contract.v1;

public record PageMetadataModel(int number, int size, long totalElements, long totalPages) {}
