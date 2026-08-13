package com.easy.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "express")
public class ExpressConfig {
    private String customer;
    private String key;
    private String secret;
    private String userid;
}
