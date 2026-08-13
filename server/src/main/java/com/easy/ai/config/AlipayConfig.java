package com.easy.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String gateway;
    private String notifyUrl;
    private String returnUrl;
    private Integer connectTimeout = 10000;
    private Integer readTimeout = 30000;
}
