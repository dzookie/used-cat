package com.easy.ai.config;

import com.easy.ai.interceptors.AdminInterceptor;
import com.easy.ai.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**", "/order/admin/**", "/commodity/admin/**", "/category/admin/**", "/knowledge/admin/**")
                .order(0);

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 用户认证相关
                        "/user/login",
                        // Swagger 相关路径
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-ui.html/**",
                        "/swagger-resources/**",
                        "/swagger-resources",
                        "/v2/api-docs",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/webjars",
                        "/doc.html",
                        "/api-docs/**",
                        "/api-docs",

                        // 静态资源
                        "/favicon.ico",
                        "/error",
                        "/static/**",
                        "/resources/**",
                        "/public/**",

                        // 其他需要排除的路径
                        "/actuator/**",
                        "/h2-console/**",
                        "/test/*",
                        "/session/*",
                        "/commodity",
                        "/commodity/getCommodityById",
                        "/commodity/getCommodityList",
                        "/commodity/search",
                        "/commodity/uploadCommunityImg",
                        "/category/*",
                        "/upload/**",
                        "/user/*",
                        "/alipay/*",
                        "/receivingAddress/*",
                        "/qq/*",
                        "/chat/**",
                        "/ws/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        String projectPath = System.getProperty("user.dir");
        String uploadPath = projectPath + "/server/upload";
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    /**
     * 配置 CorsFilter，确保 CORS 处理在拦截器之前执行
     * 这样可以正确处理 OPTIONS 预检请求
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
