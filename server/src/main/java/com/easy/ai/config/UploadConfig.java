package com.easy.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class UploadConfig {

    public static String UPLOAD_PATH;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @PostConstruct
    public void init() {
        String projectPath = System.getProperty("user.dir");
        UPLOAD_PATH = projectPath + "/server/upload";
    }

    public static String getUploadPath() {
        return UPLOAD_PATH;
    }
}
