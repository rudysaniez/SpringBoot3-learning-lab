package com.springboot.learning.sb3.controller;

import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
public class AttributeDictionaryRestController {

    static AttributeDictionaryEntity ATTR_PAYLOAD = new AttributeDictionaryEntity(
            "CODE01", "TYPE01",
            "GROUP01", true,
            true, List.of(),
            "METRIC01", "METRIC_UNIT_01",
            "REF_NAME_01", List.of(),
            10, "RULE_01",
            "REGEX_01", true,
            1.d, 10.d,
            true, true,
            "2023-01-01", "2023-12-20",
            1000, 1,
            1, true,
            true, Map.of("KEY01", "VAL01"),
            List.of(), true, "DEFAULT",
            false);

    @GetMapping(value = "/attributes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AttributeDictionaryEntity>> getById(@PathVariable(value = "code") String code) {

        return Mono.just(ATTR_PAYLOAD)
                .map(ResponseEntity::ok)
                ;
    }
}
