package com.springboot.learning.sb3.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Objects;

@Document(indexName = "videos")
public class VideoEntity {

    @Id
    private String id;

    @Field(type = FieldType.Text, name = "videoName")
    private String videoName;

    @Field(type = FieldType.Text, name = "description")
    private String description;

    @Field(type = FieldType.Text, name = "username")
    private String username;

    public VideoEntity() {}

    public VideoEntity(String id, String videoName, String description, String username) {
        this.id = id;
        this.videoName = videoName;
        this.description = description;
        this.username = username;
    }

    @Override
    public String toString() {
        return "VideoEntity{" +
               "id='" + id + '\'' +
               ", videoName='" + videoName + '\'' +
               ", description='" + description + '\'' +
               ", username='" + username + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoEntity that = (VideoEntity) o;
        return Objects.equals(videoName, that.videoName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoName);
    }

    public String getId() {
        return id;
    }

    public String getVideoName() {
        return videoName;
    }

    public String getDescription() {
        return description;
    }

    public String getUsername() {
        return username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
