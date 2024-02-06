package com.springboot.learning.sb3.mapper.v1;

import com.springboot.learning.sb3.controller.contract.AttributeDictionary;
import com.springboot.learning.sb3.controller.contract.BulkResult;
import com.springboot.learning.sb3.controller.contract.Page;
import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import com.springboot.learning.sb3.repository.impl.ReactiveOpensearchRepository;
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
