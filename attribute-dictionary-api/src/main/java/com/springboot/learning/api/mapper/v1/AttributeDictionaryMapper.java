package com.springboot.learning.api.mapper.v1;

import com.springboot.learning.api.controller.v1.AttributeDictionary;
import com.springboot.learning.api.controller.v1.BulkResult;
import com.springboot.learning.api.controller.v1.Page;
import com.springboot.learning.repository.domain.AttributeDictionaryEntity;
import com.springboot.learning.repository.impl.ReactiveOpensearchRepository;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface AttributeDictionaryMapper {

    AttributeDictionary toModel(AttributeDictionaryEntity entity);

    List<AttributeDictionary> toModelList(List<AttributeDictionaryEntity> entities);

    AttributeDictionaryEntity toEntity(AttributeDictionary model);

    List<AttributeDictionaryEntity> toEntityList(List<AttributeDictionary> model);

    Page<AttributeDictionary> toPageModel(ReactiveOpensearchRepository.Page<AttributeDictionaryEntity> pageOfEntities);

    BulkResult toBulkResultModel(ReactiveOpensearchRepository.CrudResult crudResult);

    List<BulkResult> toBulkResultModels(List<ReactiveOpensearchRepository.CrudResult> crudResult);
}
