package com.springboot.learning.api.mapper;

import com.springboot.learning.api.controller.contract.v1.AttributeDictionary;
import com.springboot.learning.dictionary.domain.AttributeDictionaryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Objects;

@Mapper
public interface AttributeDictionaryAvroMapper {

    @Mappings(value = @Mapping(target = "isReadOnly", source = "isReadOnly"))
    AttributeDictionary toAvro(AttributeDictionaryEntity entity);

    @Mappings(value = @Mapping(target = "isReadOnly", source = "isReadOnly"))
    AttributeDictionaryEntity toEntity(AttributeDictionary avro);

    default String map(CharSequence value) {
        if(Objects.isNull(value))
            return null;
        else
            return value.toString();
    }
}
