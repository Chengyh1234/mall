package com.cyh.mallgateway.filter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Files;

/**
 * 静态资源访问过滤器
 * <p>
 * 拦截 /api/uploads/** 的图片请求，直接从本地文件系统读取并返回。
 * 适用于 Spring Cloud Gateway（WebFlux）架构，替代 WebMvcConfigurer 方案。
 *
 * @author cyh
 * @since 2024-01-01
 */
@Slf4j
@Order(-200)
@Component
public class ResourceWebFilter implements WebFilter {

    @Value("${file.upload.base-path:./uploads}")
    private String basePath;

    private File baseDir;

    private static final String UPLOADS_PREFIX = "/api/uploads/";

    @PostConstruct
    public void init() {
        // 解析为绝对路径，避免相对路径依赖工作目录
        baseDir = new File(basePath).getAbsoluteFile();
        log.info("ResourceWebFilter 初始化: basePath={}, absolutePath={}, exists={}, userDir={}",
                basePath, baseDir.getAbsolutePath(), baseDir.exists(), System.getProperty("user.dir"));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 只处理 /api/uploads/ 开头的请求，其余放行
        if (!path.startsWith(UPLOADS_PREFIX)) {
            return chain.filter(exchange);
        }

        log.info("ResourceWebFilter 拦截到图片请求: {}", path);

        // 提取相对路径：/api/uploads/images/spu/xxx.jpg → images/spu/xxx.jpg
        String relativePath = path.substring(UPLOADS_PREFIX.length());
        File file = new File(baseDir, relativePath);

        log.info("解析文件路径: absolutePath={}", file.getAbsolutePath());

        if (!file.exists() || !file.isFile()) {
            log.warn("静态资源不存在: {} (path: {})", file.getAbsolutePath(), path);
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return response.setComplete();
        }

        // 确定 Content-Type
        String contentType = resolveContentType(file.getName());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        log.info("返回图片: contentType={}, fileSize={}", contentType, file.length());

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.parseMediaType(contentType));

        // 读取文件字节并返回
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("读取文件失败: {}", file.getAbsolutePath(), e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    /**
     * 根据文件扩展名解析 MIME 类型
     */
    private String resolveContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".ico")) return "image/x-icon";
        if (lower.endsWith(".bmp")) return "image/bmp";
        return null;
    }
}