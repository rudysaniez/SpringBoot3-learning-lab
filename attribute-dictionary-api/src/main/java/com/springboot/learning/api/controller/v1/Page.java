package com.springboot.learning.api.controller.v1;

import java.util.List;

public record Page<T>(List<T> content, PageMetadata pageMetadata) {}
