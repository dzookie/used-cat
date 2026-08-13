package com.easy.ai.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class QqAuthUtil {

    @Value("${qq.app-id}")
    private String appId;

    @Value("${qq.app-key}")
    private String appKey;

    @Value("${qq.authorize-url}")
    private String authorizeUrl;

    @Value("${qq.token-url}")
    private String tokenUrl;

    @Value("${qq.openid-url}")
    private String openidUrl;

    @Value("${qq.user-info-url}")
    private String userInfoUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public QqAuthUtil() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String buildAuthorizeUrl(String redirectUri, String state) {
        return authorizeUrl + "?response_type=code"
                + "&client_id=" + appId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8)
                + "&scope=get_user_info";
    }

    public String getAccessToken(String code, String redirectUri) {
        String url = tokenUrl + "?grant_type=authorization_code"
                + "&client_id=" + appId
                + "&client_secret=" + appKey
                + "&code=" + code
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&fmt=json";

        log.info("QQ获取access_token请求: {}", url);
        String response = restTemplate.getForObject(url, String.class);
        log.info("QQ获取access_token响应: {}", response);

        try {
            JsonNode json = objectMapper.readTree(response);
            if (json.has("error")) {
                log.error("QQ获取access_token失败: {}", response);
                return null;
            }
            return json.get("access_token").asText();
        } catch (Exception e) {
            log.error("QQ解析access_token异常", e);
            return null;
        }
    }

    public String getOpenId(String accessToken) {
        String url = openidUrl + "?access_token=" + accessToken + "&fmt=json";

        log.info("QQ获取openid请求: {}", url);
        String response = restTemplate.getForObject(url, String.class);
        log.info("QQ获取openid响应: {}", response);

        try {
            JsonNode json = objectMapper.readTree(response);
            if (json.has("error")) {
                log.error("QQ获取openid失败: {}", response);
                return null;
            }
            return json.get("openid").asText();
        } catch (Exception e) {
            log.error("QQ解析openid异常", e);
            return null;
        }
    }

    public QqUserInfo getUserInfo(String accessToken, String openId) {
        String url = userInfoUrl + "?access_token=" + accessToken
                + "&oauth_consumer_key=" + appId
                + "&openid=" + openId;

        log.info("QQ获取用户信息请求: {}", url);
        String response = restTemplate.getForObject(url, String.class);
        log.info("QQ获取用户信息响应: {}", response);

        try {
            JsonNode json = objectMapper.readTree(response);
            if (json.has("ret") && json.get("ret").asInt() != 0) {
                log.error("QQ获取用户信息失败: {}", response);
                return null;
            }
            String nickname = json.has("nickname") ? json.get("nickname").asText() : "QQ用户";
            String avatar = json.has("figureurl_qq_2") ? json.get("figureurl_qq_2").asText()
                    : json.has("figureurl_qq_1") ? json.get("figureurl_qq_1").asText() : "";
            return new QqUserInfo(openId, nickname, avatar);
        } catch (Exception e) {
            log.error("QQ解析用户信息异常", e);
            return null;
        }
    }

    public record QqUserInfo(String openId, String nickname, String avatar) {}
}
