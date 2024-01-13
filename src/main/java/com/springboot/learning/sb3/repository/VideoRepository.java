package com.springboot.learning.sb3.repository;

import com.springboot.learning.sb3.domain.VideoEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VideoRepository extends ReactiveCrudRepository<VideoEntity, Long> {

    Flux<VideoEntity> findTop3ByNameContainsIgnoreCaseOrderByName(String name);

    Flux<VideoEntity> findTop3ByDescriptionContainingIgnoreCaseOrderByName(String description);

    Flux<VideoEntity> findByName(String name);

    @PreAuthorize(value = "#entity.username == authentication.name")
    @Override
    Mono<Void> delete(VideoEntity entity);

    @Modifying
    @Query(value = "delete from video where name=:name")
    Mono<Integer> deleteByName(@Param("name") String name);

    @Query(value = "select v.* from video v offset :offset limit :limit")
    Flux<VideoEntity> findAllAsPage(int offset, int limit);
}
