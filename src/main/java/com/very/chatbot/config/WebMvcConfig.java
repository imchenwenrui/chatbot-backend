package com.very.chatbot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 *
 * 注册两项拦截器,分别是 UserContextInterceptor 用于提取 X-User-Id,
 * 以及 RateLimitInterceptor 用于单用户每分钟限流。
 *
 * 另开启 CORS,允许前端 dev server (localhost:5173) 与后续生产域名访问。
 *
 * @author chatbot
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/api/**");
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                // SSE 长连接自己限流,避免双重计数
                .excludePathPatterns("/api/conversations/*/messages");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-User-Id")
                // 本期不接 cookie / session,直接 false;后续登录时再按域白名单开
                .allowCredentials(false)
                .maxAge(3600);
    }
}
