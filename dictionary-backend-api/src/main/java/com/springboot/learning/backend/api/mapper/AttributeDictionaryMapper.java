package com.springboot.learning.backend.api.mapper;

import com.adeo.pro.replenishment.api.dictionary.model.AttributeDictionary;
import com.adeo.pro.replenishment.api.dictionary.model.PageAttributeDictionary;
import com.springboot.learning.backend.api.controller.contract.v1.AttributeDictionaryModel;
import com.springboot.learning.backend.api.controller.contract.v1.PageModel;
import org.mapstruct.Mapper;

@Mapper
public interface AttributeDictionaryMapper {

    AttributeDictionaryModel toModel(AttributeDictionary attributeDictionary);

    PageModel<AttributeDictionaryModel> toPageModel(PageAttributeDictionary pageAttributeDictionary);
}
