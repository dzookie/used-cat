package com.easy.ai.interceptors;

import com.easy.ai.entity.User;
import com.easy.ai.service.UserService;
import com.easy.ai.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        try {
            if (token == null || token.isEmpty()) {
                writeErrorResponse(response, 401, "未登录");
                return false;
            }

            Claims claims = JwtUtil.parseToken(token);
            String email = claims.getSubject();
            User user = userService.findUserByEmail(email);

            if (user == null) {
                writeErrorResponse(response, 401, "用户不存在");
                return false;
            }

            if (user.getRole() == null || user.getRole() != 1) {
                writeErrorResponse(response, 403, "无管理员权限");
                return false;
            }

            return true;
        } catch (Exception e) {
            writeErrorResponse(response, 401, "Token无效或已过期");
            return false;
        }
    }

    private void writeErrorResponse(HttpServletResponse response, int code, String message) throws Exception {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
