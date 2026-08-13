package com.easy.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Getter
@Component
public class AiPromptConfig {

    private final ResourceLoader resourceLoader;

    private static final Map<String, Supplier<String>> SYSTEM_VARIABLES = Map.of(
            "currentDate", () -> LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
            "currentTime", () -> LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")),
            "currentYear", () -> String.valueOf(LocalDateTime.now().getYear()),
            "currentWeekday", () -> LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE"))
    );

    private String assistant;
    private String customerService;

    public AiPromptConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        assistant = loadPrompt("classpath:prompts/assistant.txt", "你是AI助理，小D");
        customerService = loadPrompt("classpath:prompts/customer-service.txt", "你是二手猫闲置交易平台的AI智能客服");
        log.info("AI提示词加载完成");
    }

    private String loadPrompt(String location, String fallback) {
        try {
            Resource resource = resourceLoader.getResource(location);
            if (resource.exists()) {
                return resource.getContentAsString(StandardCharsets.UTF_8).trim();
            }
        } catch (IOException e) {
            log.warn("加载提示词文件失败: {}", location, e);
        }
        log.warn("提示词文件不存在，使用默认值: {}", location);
        return fallback;
    }

    public String resolve(String template) {
        String result = template;
        for (Map.Entry<String, Supplier<String>> entry : SYSTEM_VARIABLES.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue().get());
        }
        return result;
    }
}
