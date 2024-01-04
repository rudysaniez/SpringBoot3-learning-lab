package com.adeo.springboot.learning.sb3.mapper;

import com.adeo.springboot.learning.sb3.domain.VideoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface VideoMapper {

    @Mappings(value = @Mapping(target = "id", ignore = true))
    VideoEntity toEntity(com.adeo.springboot.learning.sb3.dto.Video video);

    com.adeo.springboot.learning.sb3.dto.Video toModel(VideoEntity video);
}
