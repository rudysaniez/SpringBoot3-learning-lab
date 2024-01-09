package com.springboot.learning.sb3.repository;

import com.springboot.learning.sb3.domain.VideoEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface VideoListRepository extends ListCrudRepository<VideoEntity, Long> {}
