package com.adeo.springboot.learning.sb3.repository;

import com.adeo.springboot.learning.sb3.domain.VideoEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends CrudRepository<VideoEntity, Long> {

    List<VideoEntity> findTop3ByNameContainsIgnoreCaseOrderByName(String name);

    List<VideoEntity> findTop3ByDescriptionContainingIgnoreCaseOrderByName(String description);

    @Modifying
    @Query(value = "delete from video where name=:name")
    int deleteByName(@Param("name") String name);
}
