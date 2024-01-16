package com.springboot.learning.sb3.repository;

import com.springboot.learning.sb3.domain.VideoEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends ElasticsearchRepository<VideoEntity, Long> {

    List<VideoEntity> findByVideoNameLikeIgnoreCase(String name);

    VideoEntity findByVideoNameAndUsername(String name, String username);
}
