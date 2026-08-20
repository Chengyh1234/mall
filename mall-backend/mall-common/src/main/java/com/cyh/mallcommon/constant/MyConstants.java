package com.cyh.mallcommon.constant;

/**
 * 系统常量类
 * <p>
 * 统一管理系统中使用的非 Redis 静态常量
 *
 * @author cyh
 * @since 2024-01-01
 */
public class MyConstants {

    private MyConstants() {
        // 静态工具类，禁止实例化
    }

    // ==================== 请求头常量 ====================

    /**
     * 认证请求头名称
     */
    public static final String AUTH_HEADER = "Authorization";

    /**
     * Token请求头前缀
     * 完整格式：Bearer xxx
     */
    public static final String BEARER_PREFIX = "Bearer ";

    }