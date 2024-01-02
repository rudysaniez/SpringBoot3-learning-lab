package com.adeo.springboot.learning.sb3.controller;

import com.adeo.springboot.learning.sb3.dto.Video;
import com.adeo.springboot.learning.sb3.dto.VideoSearch;
import com.adeo.springboot.learning.sb3.mapper.VideoMapper;
import com.adeo.springboot.learning.sb3.service.VideoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.stream.StreamSupport;

@Controller
public class VideoWebController {

    private final VideoService videoService;
    private final VideoMapper videoMapper;

    public VideoWebController(VideoService videoService,
                              VideoMapper videoMapper) {
        this.videoService = videoService;
        this.videoMapper = videoMapper;
    }

    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute("videos", videoService.findAll(0, 10).getContent().stream()
                .map(videoMapper::toModel)
                .toList());
        return "index";
    }

    @GetMapping("/react")
    public String react() {
        return "react";
    }

    @PostMapping("/new-video")
    public String newVideo(@ModelAttribute Video videoName, Authentication authentication) {

        synchronized (this) {
            videoService.save(videoName, authentication.getName());
        }

        return "redirect:/";
    }

    @PostMapping("/search-video")
    public String searchVideo(@ModelAttribute VideoSearch videoSearched, Model model) {

        synchronized (this) {
            model.addAttribute("videosFound",
                videoService.search(videoSearched).stream()
                    .map(videoMapper::toModel)
                    .toList()
            );
        }
        return "index";
    }
}
