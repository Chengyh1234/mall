package com.cyh.mallcommon.config;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 统一配置
 * <p>
 * 配置超时、重试、日志等
 * <p>
 * 注意：不配置请求拦截器自动传播用户请求头（X-User-Id/X-User-Roles），
 * 内部 Feign 调用为服务间通信，不应伪造用户身份上下文。
 * 必要的上下文通过方法参数显式传递。
 */
@Configuration
public class FeignConfig {

    /**
     * Feign 超时配置：连接超时 5s，读取超时 10s
     */
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(5000, 10000);
    }

    /**
     * Feign 重试配置：最多重试 3 次，初始间隔 1s，最大间隔 3s，乘数 1.5
     * <p>
     * 默认不重试，加上重试机制可应对临时性网络抖动
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
                1000L,   // period（初始间隔 1s）
                3000L,   // maxPeriod（最大间隔 3s）
                3        // maxAttempts（最多重试 3 次，含首次调用）
        );
    }

    /**
     * Feign 日志级别：BASIC（记录请求方法、URL、响应状态码、执行时间）
     * <p>
     * 如需更详细日志可改为 FULL，生产环境建议 BASIC
     */
    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.BASIC;
    }
}