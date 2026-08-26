package com.cyh.mallgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * 全局跨域配置
 * <p>
 * 使用 {@link CorsWebFilter} 在 WebFlux 层面统一处理 CORS 预检请求（OPTIONS），
 * 确保所有来源、方法、请求头均被允许，避免浏览器因预检失败而阻止实际请求。
 * <p>
 * 注意：此配置替代 application.yml 中的 globalcors 配置（后者在 WebFlux 中效果不佳）。
 *
 * @author cyh
 * @since 2024-01-01
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setMaxAge(7200L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}