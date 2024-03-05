package com.springboot.learning.common;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class OpensearchHelper {

    private static final Logger log = LoggerFactory.getLogger(OpensearchHelper.class);

    /**
     * @param index : the opensearch mapping
     * @param indexName : The index name
     * @param baseUrl : the opensearch base URL
     * @return {@link OpenSearchIndexCreationResult}
     */
    public static Optional<OpenSearchIndexCreationResult> putIndex(@NotNull Resource index,
                                                                   @NotNull String indexName,
                                                                   @NotNull String baseUrl) {

        try {
            final var IDX = index.getContentAsString(StandardCharsets.UTF_8);
            log.debug(" > This index is read {}", IDX);

            OpenSearchIndexCreationResult result = WebClient.builder().baseUrl(baseUrl)
                    .build()
                    .put()
                    .uri(uriBuilder -> uriBuilder.pathSegment(indexName).build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(IDX)
                    .retrieve()
                    .bodyToMono(OpenSearchIndexCreationResult.class)
                    .block();

            log.info(" > Index creation {}", result);

            return Optional.ofNullable(result);
        }
        catch(IOException e) {
            log.error(e.getMessage(), e);
        }

        return Optional.empty();
    }

    /**
     * @param baseUrl : the opensearch base URL
     * @return {@link OpenSearchIndexCreationResult}
     */
    public static Optional<OpenSearchIndexCreationResult> putIndexV1(String baseUrl) {
        return OpensearchHelper.putIndex(
                new ClassPathResource("index/index_attribute_dictionary_v1.json"),
                "attributes_dictionary_v1",
                baseUrl
        );
    }

    public record OpenSearchIndexCreationResult(boolean acknowledged, boolean shards_acknowledged, String index) {}
}
