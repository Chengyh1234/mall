package com.cyh.mallportal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Boot 全局跨域配置
 */
@Configuration // 标记为配置类，Spring 启动时⾃动加载
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 对所有请求路径（/**）应⽤跨域策略
        registry.addMapping("/**")
                // 【开发阶段】允许所有源跨域（⽅便本地测试，⽆需指定具体域名）
                .allowedOriginPatterns("*")
                //.allowedOriginPatterns("http://localhost:5176", "http://localhost:5173")
                // 【⽣产环境】严格指定信任的前端域名，避免恶意⽹站滥⽤（示例）
                // .allowedOriginPatterns("https://vue3-frontend.example.com","https://admin.example.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的 HTTP 请求⽅法
                .allowedHeaders("*") // 允许所有请求头（如 Token、Content-Type 等）
                .allowCredentials(true) // 是否允许携带凭证（如 Cookie、Token）
                .maxAge(3600); // 预检请求（OPTIONS）的缓存时间（秒），减少预检请求次数
    }
}