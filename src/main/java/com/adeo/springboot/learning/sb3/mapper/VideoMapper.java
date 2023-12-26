package com.adeo.springboot.learning.sb3.mapper;

import com.adeo.springboot.learning.sb3.domain.VideoEntity;
import org.mapstruct.Mapper;

@Mapper
public interface VideoMapper {

    VideoEntity toEntity(com.adeo.springboot.learning.sb3.dto.Video video);

    com.adeo.springboot.learning.sb3.dto.Video toModel(VideoEntity video);
}
