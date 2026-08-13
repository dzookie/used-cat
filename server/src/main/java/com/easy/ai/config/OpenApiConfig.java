package com.easy.ai.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearer-jwt";
        
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"");
        
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, securityScheme))
                // 关键：全局应用安全方案，这样 Swagger UI 才会在请求中带上 Header
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}

