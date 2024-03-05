package com.springboot.learning.backend.api.controller.contract.v1;

import java.util.List;

public record PageModel<T>(List<T> content, PageMetadataModel pageMetadata) {}
