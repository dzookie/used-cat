package com.easy.ai.interceptors;

import com.easy.ai.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理 OPTIONS 预检请求，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        //令牌验证
        String token = request.getHeader("Authorization");
        try {
            if (token == null) {
                throw new RuntimeException();
            }

            // 验证 token 是否有效，如果无效会抛出异常
            JwtUtil.parseToken(token);

            return true;
        }catch (Exception e){
            // 设置 CORS 响应头，避免跨域错误
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setStatus(401);
            return false;
        }
    }
}
