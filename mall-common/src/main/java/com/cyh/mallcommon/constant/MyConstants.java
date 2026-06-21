package com.cyh.mallcommon.constant;

/**
 * 系统常量类
 * <p>
 * 统一管理系统中使用的静态常量，包括：
 * 1. Token相关常量
 * 2. 请求头常量
 * 3. 文件上传相关常量
 * 4. 缓存相关常量
 *
 * @author cyh
 * @since 2024-01-01
 */
public class MyConstants {

    private MyConstants() {
        // 静态工具类，禁止实例化
    }

    // ==================== Token相关常量 ====================

    /**
     * Redis中Token存储的前缀
     * 完整key格式：token:{token值}
     */
    public static final String TOKEN_PREFIX = "token:";

    /**
     * Token过期时间（秒）
     * 默认24小时：86400秒
     */
    public static final long TOKEN_EXPIRATION = 86400L;

    /**
     * 用户当前会话Redis Key前缀
     * 完整格式：user:current_session:{userId}
     * 用于：单点登录（SSO）强制踢下线
     */
    public static final String USER_CURRENT_SESSION_PREFIX = "user:current_session:";

    /**
     * 用户活跃Token映射Redis Key前缀
     * 完整格式：user:active_token:{userId}
     * 用于通过userId快速找到其对应的token，便于权限变更时原地更新Redis缓存,即时踢下线或刷新权限缓存
     *
     * 用于：后台权限变更的索引
     */
    public static final String USER_ACTIVE_TOKEN_PREFIX = "user:active_token:";

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



    // ==================== 验证码相关常量 ====================

    /**
     * 验证码 Redis Key 前缀
     * 完整格式：captcha:{captchaKey}
     */
    public static final String CAPTCHA_PREFIX = "captcha:";

    /**
     * 验证码过期时间（秒）
     * 默认5分钟：300秒
     */
    public static final long CAPTCHA_EXPIRATION = 300L;

    // ==================== 缓存相关常量 ====================

    /**
     * SPU列表缓存键前缀
     * 完整格式：spu:page:c:{categoryId}:b:{brandId}:k:{keywordHash}:s:{status}:p:{page}:ps:{pageSize}
     */
    public static final String SPU_CACHE_PREFIX = "spu:page:";

    /**
     * SPU总数缓存键前缀
     * 完整格式：spu:count:c:{categoryId}:b:{brandId}:k:{keywordHash}:s:{status}
     */
    public static final String SPU_COUNT_PREFIX = "spu:count:";

    /**
     * SPU缓存过期时间（分钟）
     * 设置为30分钟，平衡缓存命中率和数据新鲜度
     */
    public static final int SPU_CACHE_EXPIRE_MINUTES = 30;

    // ==================== 邮箱验证码相关常量 ====================

    /**
     * 登录邮箱验证码 Redis Key 前缀
     * 完整格式：email:login:code:{email}
     */
    public static final String EMAIL_LOGIN_CODE_PREFIX = "email:login:code:";

    /**
     * 注册邮箱验证码 Redis Key 前缀
     * 完整格式：email:register:code:{email}
     */
    public static final String EMAIL_REGISTER_CODE_PREFIX = "email:register:code:";

    /**
     * 重置密码邮箱验证码 Redis Key 前缀
     * 完整格式：email:reset:pwd:{email}
     */
    public static final String EMAIL_RESET_PWD_CODE_PREFIX = "email:reset:pwd:";

    /**
     * 邮箱验证码过期时间（秒）
     * 默认1分钟：60秒
     */
    public static final long EMAIL_CODE_EXPIRATION = 60L;

    // ==================== Banner轮播图相关常量 ====================

    /**
     * 轮播图缓存键前缀
     */
    public static final String BANNER_CACHE_PREFIX = "banner:";
}
