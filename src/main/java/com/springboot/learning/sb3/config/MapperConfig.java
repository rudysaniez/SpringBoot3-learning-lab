package com.springboot.learning.sb3.config;

import com.springboot.learning.sb3.mapper.VideoMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MapperConfig {

    @Bean
    VideoMapper videoMapper() {
        return Mappers.getMapper(VideoMapper.class);
    }
}
