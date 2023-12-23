package com.adeo.springboot.learning.sb3.dto;

import java.util.List;

public record Page(List<Video> content, PageMetadata pageMetadata) {}
