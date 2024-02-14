package com.springboot.learning.api.mapper;

import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnary;
import com.springboot.learning.repository.domain.AttributeDictionaryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Objects;

@Mapper
public interface AttributeDictionaryAvroMapper {

    @Mappings(value = @Mapping(target = "isReadOnly", source = "isReadOnly"))
    AttributeDictionnary toAvro(AttributeDictionaryEntity entity);

    @Mappings(value = @Mapping(target = "isReadOnly", source = "isReadOnly"))
    AttributeDictionaryEntity toEntity(AttributeDictionnary avro);

    default String map(CharSequence value) {
        if(Objects.isNull(value))
            return null;
        else
            return value.toString();
    }
}
