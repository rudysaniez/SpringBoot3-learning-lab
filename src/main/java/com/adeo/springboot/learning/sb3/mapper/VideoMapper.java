package com.adeo.springboot.learning.sb3.mapper;

import com.adeo.springboot.learning.sb3.domain.Video;
import org.mapstruct.Mapper;

@Mapper
public interface VideoMapper {

    Video toEntity(com.adeo.springboot.learning.sb3.dto.Video video);

    com.adeo.springboot.learning.sb3.dto.Video toModel(Video video);
}
