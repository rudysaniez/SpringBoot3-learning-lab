package com.springboot.learning.backend.api.controller;

import com.adeo.pro.replenishment.api.dictionary.model.AttributeDictionary;
import com.adeo.pro.replenishment.api.dictionary.model.PageAttributeDictionary;
import com.springboot.learning.backend.api.config.PropertiesBackendConfig;
import com.springboot.learning.backend.api.controller.contract.v1.AttributeDictionaryModel;
import com.springboot.learning.backend.api.controller.contract.v1.BackendAttributeAPI;
import com.springboot.learning.backend.api.controller.contract.v1.PageModel;
import com.springboot.learning.backend.api.integration.AttributeDictionaryIntegration;
import com.springboot.learning.backend.api.mapper.AttributeDictionaryMapper;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class BackendAttributeController implements BackendAttributeAPI {

    private final AttributeDictionaryIntegration attributeDictionaryIntegration;
    private final PropertiesBackendConfig.Pagination pagination;

    private static final AttributeDictionaryMapper attributeDictionaryMapper = Mappers.getMapper(AttributeDictionaryMapper.class);

    private static final Logger log = LoggerFactory.getLogger(BackendAttributeController.class);

    public BackendAttributeController(AttributeDictionaryIntegration attributeDictionaryIntegration,
                                      PropertiesBackendConfig.Pagination pagination) {

        this.attributeDictionaryIntegration = attributeDictionaryIntegration;
        this.pagination = pagination;
    }

    /**
     * @param id : the attribute identifier
     * @param exchange : the server web exchange
     * @return {@link AttributeDictionaryModel}
     */
    @Override
    public Mono<ResponseEntity<AttributeDictionaryModel>> getAttributeById(String id,
                                                                           ServerWebExchange exchange) {

        log.info(" > Get dictionary attributes by identifier {}", id);

        return attributeDictionaryIntegration.getAttributeById(id, exchange)
            .<AttributeDictionary>handle((response, sink) -> {
                if(response.getBody() != null)
                    sink.next(response.getBody());
            })
            .map(attributeDictionaryMapper::toModel)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * @param page : the page
     * @param size : the size
     * @param exchange : the server web exchange
     * @return {@link PageModel<AttributeDictionaryModel>}
     */
    @Override
    public Mono<ResponseEntity<PageModel<AttributeDictionaryModel>>> getAttributesAsPage(int page,
                                                                                         int size,
                                                                                         ServerWebExchange exchange) {

        log.info(" > Get dictionary attributes with page={} and size={}", page, size);

        final int p = Math.max(page, 0);
        final int s = Math.min(size, pagination.size());

        return attributeDictionaryIntegration.getAllAttributes(p, s, exchange)
            .<PageAttributeDictionary>handle((response, sink) -> {
                if(response.getBody() != null)
                    sink.next(response.getBody());
            })
            .map(attributeDictionaryMapper::toPageModel)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.noContent().build());
    }
}
