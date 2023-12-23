package com.adeo.springboot.learning.sb3.dto;

import java.util.List;

public record PageVideo(List<Video> content, PageMetadata pageMetadata) {}
