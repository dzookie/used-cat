package com.easy.ai.controller;

import com.easy.ai.common.Result;
import com.easy.ai.entity.User;
import com.easy.ai.service.UserService;
import com.easy.ai.utils.JwtUtil;
import com.easy.ai.utils.QqAuthUtil;
import com.easy.ai.utils.QqAuthUtil.QqUserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/qq")
@Tag(name = "QQ登录", description = "QQ第三方登录相关接口")
public class QqAuthController {

    @Autowired
    private QqAuthUtil qqAuthUtil;

    @Autowired
    private UserService userService;

    @Value("${qq.frontend-url}")
    private String frontendUrl;

    @Value("${qq.redirect-uri}")
    private String redirectUri;

    @GetMapping("/authorize")
    @Operation(summary = "QQ授权跳转", description = "跳转到QQ授权页面")
    public void authorize(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String url = qqAuthUtil.buildAuthorizeUrl(redirectUri, state);
        log.info("QQ授权跳转URL: {}", url);
        response.sendRedirect(url);
    }

    @GetMapping("/callback")
    @Operation(summary = "QQ授权回调", description = "处理QQ授权回调，完成登录")
    public void callback(@RequestParam("code") String code,
                         @RequestParam(value = "state", required = false) String state,
                         HttpServletResponse response) throws IOException {
        log.info("QQ回调收到code: {}", code);

        String accessToken = qqAuthUtil.getAccessToken(code, redirectUri);
        if (accessToken == null) {
            response.sendRedirect(frontendUrl + "/login?error=token_failed");
            return;
        }

        String openId = qqAuthUtil.getOpenId(accessToken);
        if (openId == null) {
            response.sendRedirect(frontendUrl + "/login?error=openid_failed");
            return;
        }

        QqUserInfo qqUserInfo = qqAuthUtil.getUserInfo(accessToken, openId);
        if (qqUserInfo == null) {
            response.sendRedirect(frontendUrl + "/login?error=userinfo_failed");
            return;
        }

        User user = userService.findUserByOpenId(openId);
        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setProvider("qq");
            user.setEmail("qq_" + openId.substring(0, 10) + "@qq.login");
            user.setPassword("");
            user.setNickname(qqUserInfo.nickname());
            user.setAvatar(qqUserInfo.avatar());
            user.setRole(2);
            user.setCredit(0);
            user.setCreateTime(LocalDateTime.now());
            userService.save(user);
            log.info("QQ登录创建新用户: openId={}, nickname={}", openId, qqUserInfo.nickname());
        } else {
            if (qqUserInfo.nickname() != null && !qqUserInfo.nickname().isEmpty()) {
                user.setNickname(qqUserInfo.nickname());
            }
            if (qqUserInfo.avatar() != null && !qqUserInfo.avatar().isEmpty()) {
                user.setAvatar(qqUserInfo.avatar());
            }
            userService.updateById(user);
            log.info("QQ登录更新已有用户: userId={}, nickname={}", user.getUserId(), user.getNickname());
        }

        String token = JwtUtil.getToken(user);
        String redirectUrl = frontendUrl + "/login?token=" + URLEncoder.encode("Bearer " + token, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
    }
}
