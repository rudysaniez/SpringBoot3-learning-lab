package com.springboot.learning.api.controller.contract.v1;

public record PageMetadata(int number, int size, long totalElements, long totalPages) {}
