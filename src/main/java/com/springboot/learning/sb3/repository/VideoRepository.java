package com.springboot.learning.sb3.repository;

import com.springboot.learning.sb3.domain.VideoEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface VideoRepository extends CrudRepository<VideoEntity, Long> {

    List<VideoEntity> findTop3ByNameContainsIgnoreCaseOrderByName(String name);

    List<VideoEntity> findTop3ByDescriptionContainingIgnoreCaseOrderByName(String description);

    List<VideoEntity> findByName(String name);

    @PreAuthorize(value = "#entity.username == authentication.name")
    @Override
    void delete(VideoEntity entity);

    @Modifying
    @Query(value = "delete from video where name=:name")
    int deleteByName(@Param("name") String name);
}
