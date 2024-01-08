package com.springboot.learning.sb3.mapper;

import com.springboot.learning.sb3.domain.VideoEntity;
import com.springboot.learning.sb3.dto.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface VideoMapper {

    @Mappings(value = @Mapping(target = "id", ignore = true))
    VideoEntity toEntity(Video video);

    Video toModel(VideoEntity video);
}
