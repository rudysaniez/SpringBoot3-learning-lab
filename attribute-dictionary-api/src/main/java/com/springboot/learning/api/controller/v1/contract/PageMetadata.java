package com.springboot.learning.api.controller.v1.contract;

public record PageMetadata(int number, int size, long totalElements, long totalPages) {}
