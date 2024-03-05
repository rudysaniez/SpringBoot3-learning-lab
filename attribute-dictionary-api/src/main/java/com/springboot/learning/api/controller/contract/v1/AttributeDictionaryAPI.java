package com.springboot.learning.api.controller.contract.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "AttributeDictionaryRestController", description = "Reactive REST API for attribute dictionary")
public interface AttributeDictionaryAPI {

    @Operation(
            operationId = "getAttributeById",
            summary = "Get attributes by identifier.",
            description = "Get attributes by identifier.",
            tags = {"AttributeDictionaryRestController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The attribute as been found.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "204", description = "The attribute does not exist.", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/attributes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<AttributeDictionary>> getAttributeById(@PathVariable(value = "id") String id);


    @Operation(
            operationId = "getAllAttributes",
            summary = "Get attributes in page.",
            description = "Get attributes in page.",
            tags = {"AttributeDictionaryRestController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The attributes are retrieves in a page"),
            @ApiResponse(responseCode = "204", description = "The attributes does not exist")
    })
    @GetMapping(value = "/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Page<AttributeDictionary>>> getAttributesAsPage(@RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                                     @RequestParam(value = "size", defaultValue = "5", required = false) int size);


    @Operation(
            operationId = "searchAttributes",
            summary = "Search attributes.",
            description = "Search attributes.",
            tags = {"AttributeDictionaryRestController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The search is performed.")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/attributes/:search", produces = MediaType.APPLICATION_JSON_VALUE)
    Flux<AttributeDictionary> searchAttributes(@RequestParam(value = "q") String q);


    @Operation(
            operationId = "saveAttribute",
            summary = "Save an attribute.",
            description = "Save an attribute.",
            tags = {"AttributeDictionaryRestController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The attribute is created."),
            @ApiResponse(responseCode = "422", description = "The input attribute is incorrect.")
    })
    @PostMapping(value = "/attributes",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<AttributeDictionary>> saveAttribute(@RequestBody AttributeDictionary attributeDictionary);


    @Operation(
            operationId = "saveAttributeAsync",
            summary = "Save an attribute asynchronously.",
            description = "Save an attribute asynchronously.",
            tags = {"AttributeDictionaryRestController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "The attribute creation has been accepted."),
            @ApiResponse(responseCode = "422", description = "The input attribute is incorrect.")
    })
    @PostMapping(value = "/attributes/:async",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Void>> saveAttributeAsync(@RequestBody AttributeDictionary attributeDictionaries);


    @Operation(
            operationId = "updateAttribute",
            summary = "Update one attribute.",
            description = "Update one attribute.",
            tags = {"AttributeDictionaryRestController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The updating of attribute has been performed."),
            @ApiResponse(responseCode = "422", description = "The input attribute are incorrect.")
    })
    @PutMapping(value = "/attributes/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<AttributeDictionary>> updateAttribute(@PathVariable(value = "id") String id,
                                                              @RequestBody AttributeDictionary attributeDictionary);


    @Operation(
            operationId = "deleteOneAttribute",
            summary = "Delete one attribute.",
            description = "Delete one attribute.",
            tags = {"AttributeDictionaryRestController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The attribute deletion has been performed."),
            @ApiResponse(responseCode = "204", description = "Nothing deletion has been performed.")
    })
    @DeleteMapping(value = "/attributes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Void>> deleteOneAttribute(@PathVariable(value = "id") String id);


    @Operation(
            operationId = "deleteAllAttributes",
            summary = "Delete all attributes.",
            description = "Delete all attributes.",
            tags = {"AttributeDictionaryRestController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The attributes deletion has been performed."),
            @ApiResponse(responseCode = "204", description = "Nothing deletions has been performed.")
    })
    @DeleteMapping(value = "/attributes/:empty", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Long>> deleteAllAttributes();
}
