package com.springboot.learning.sb3.cucumber.glues.config;

import org.springframework.boot.test.web.server.LocalServerPort;

public class GlueConfiguration {

    @LocalServerPort
    private int port;

    public int getPort() {
        return port;
    }
}
