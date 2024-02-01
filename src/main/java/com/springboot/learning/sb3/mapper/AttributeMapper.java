package com.springboot.learning.sb3.mapper;

import com.example.pennyworth.replenishment.referential.synchronisation.event.v1.AttributeDictionnary;
import com.springboot.learning.sb3.domain.AttributeDictionaryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Objects;

@Mapper
public interface AttributeMapper {

    @Mappings(value = @Mapping(target = "isReadOnly", source = "readOnly"))
    AttributeDictionnary toAvro(AttributeDictionaryEntity entity);

    @Mappings(value = @Mapping(target = "readOnly", source = "isReadOnly"))
    AttributeDictionaryEntity toModel(AttributeDictionnary avro);

    default String map(CharSequence value) {
        if(Objects.isNull(value))
            return null;
        else
            return value.toString();
    }
}
