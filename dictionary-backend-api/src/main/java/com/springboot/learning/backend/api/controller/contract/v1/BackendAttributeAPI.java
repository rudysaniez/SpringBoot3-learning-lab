package com.springboot.learning.backend.api.controller.contract.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Tag(name = "BackendAttributeController", description = "Reactive REST API for Bonsai backend")
public interface BackendAttributeAPI {

    @Operation(
            operationId = "getAttributeById",
            summary = "Get attributes by identifier.",
            description = "Get attributes by identifier.",
            tags = {"BackendAttributeController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The attribute as been found."),
            @ApiResponse(responseCode = "204", description = "The attribute does not exist.")
    })
    @GetMapping(value = "/dictionary/attributes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<AttributeDictionaryModel>> getAttributeById(
        @PathVariable(value = "id") String id,
        ServerWebExchange exchange);


    @Operation(
            operationId = "getAllAttributes",
            summary = "Get attributes in page.",
            description = "Get attributes in page.",
            tags = {"BackendAttributeController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The attributes are retrieves in a page"),
            @ApiResponse(responseCode = "204", description = "The attributes does not exist")
    })
    @GetMapping(value = "/dictionary/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<PageModel<AttributeDictionaryModel>>> getAttributesAsPage(
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "5", required = false) int size,
        ServerWebExchange exchange);
}
