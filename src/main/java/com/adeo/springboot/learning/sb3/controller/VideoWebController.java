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

@Controller
public class VideoWebController {

    private final VideoService videoService;
    private final VideoMapper videoMapper;

    public VideoWebController(VideoService videoService,
                              VideoMapper videoMapper) {
        this.videoService = videoService;
        this.videoMapper = videoMapper;
    }

    /**
     * @param model : the model
     * @return {@link String mustache template}
     */
    @GetMapping(value = {"/", "/web", "/web/videos"})
    public String index(Model model) {

        model.addAttribute("videos", videoService.findAll(0, 10).getContent().stream()
                .map(videoMapper::toModel)
                .toList());
        return "index";
    }

    /**
     * @param videoSearched: the video searched
     * @param model : the model
     * @return {@link String mustache template}
     */
    @PostMapping("/web/videos/:search")
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

    /**
     *
     * @param video : the video to create
     * @param authentication : the authentication user
     * @return {@link String mustache template}
     */
    @PostMapping("/web/videos/:create")
    public String newVideo(@ModelAttribute Video video, Authentication authentication) {
        videoService.save(video, authentication.getName());
        return "redirect:/web";
    }
}
