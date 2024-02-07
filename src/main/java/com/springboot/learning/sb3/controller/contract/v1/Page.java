package com.springboot.learning.sb3.controller.contract.v1;

import java.util.List;

public record Page<T>(List<T> content, PageMetadata pageMetadata) {}
