package com.cyh.mallcommon.constant;

/**
 * Redis 相关常量类
 * <p>
 * 统一管理所有 Redis Key 前缀、缓存 TTL 等常量
 *
 * @author cyh
 * @since 2024-01-01
 */
public class RedisConstants {

    private RedisConstants() {
        // 静态工具类，禁止实例化
    }

    // ==================== Token 相关 ====================

    /** Redis 中 Token 存储前缀，完整 key 格式：token:{token值} */
    public static final String TOKEN_PREFIX = "token:";

    /** Token 过期时间（秒），默认 24 小时 */
    public static final long TOKEN_EXPIRATION = 86400L;

    /** 用户当前会话 Redis Key 前缀，完整格式：user:current_session:{userId} */
    public static final String USER_CURRENT_SESSION_PREFIX = "user:current_session:";

    /** 用户活跃 Token 映射 Redis Key 前缀，完整格式：user:active_token:{userId} */
    public static final String USER_ACTIVE_TOKEN_PREFIX = "user:active_token:";

    // ==================== 图形验证码相关 ====================

    /** 验证码 Redis Key 前缀，完整格式：captcha:{captchaKey} */
    public static final String CAPTCHA_PREFIX = "captcha:";

    /** 验证码过期时间（秒），默认 5 分钟 */
    public static final long CAPTCHA_EXPIRATION = 60L;

    // ==================== 邮箱验证码相关 ====================

    /** 登录邮箱验证码 Redis Key 前缀，完整格式：email:login:code:{email} */
    public static final String EMAIL_LOGIN_CODE_PREFIX = "email:login:code:";

    /** 注册邮箱验证码 Redis Key 前缀，完整格式：email:register:code:{email} */
    public static final String EMAIL_REGISTER_CODE_PREFIX = "email:register:code:";

    /** 重置密码邮箱验证码 Redis Key 前缀，完整格式：email:reset:pwd:{email} */
    public static final String EMAIL_RESET_PWD_CODE_PREFIX = "email:reset:pwd:";

    /** 邮箱验证码过期时间（秒），默认 5 分钟 */
    public static final long EMAIL_CODE_EXPIRATION = 80L;

    // ==================== SPU 缓存相关 ====================

    /** SPU 详情缓存 Key 前缀（公开），完整格式：spu:detail:{spuId} */
    public static final String SPU_DETAIL_PREFIX = "spu:detail:";

    /** SPU 详情缓存 Key 前缀（商家），完整格式：spu:detail:seller:{spuId} */
    public static final String SPU_DETAIL_SELLER_PREFIX = "spu:detail:seller:";

    /** SPU 详情缓存 Key 前缀（管理），完整格式：spu:detail:admin:{spuId} */
    public static final String SPU_DETAIL_ADMIN_PREFIX = "spu:detail:admin:";

    /** SPU 详情公开缓存 TTL（分钟），默认 30 分钟 */
    public static final int SPU_DETAIL_CACHE_TTL_PUBLIC = 30;

    /** SPU 详情商家/管理缓存 TTL（分钟），默认 10 分钟 */
    public static final int SPU_DETAIL_CACHE_TTL_MGMT = 10;

    // ==================== 轮播图缓存相关 ====================

    /** 轮播图缓存键前缀 */
    public static final String BANNER_CACHE_PREFIX = "banner:";

    /** 启用的轮播图列表缓存键 */
    public static final String BANNER_ACTIVE_KEY = "banner:active";

    // ==================== 分类缓存相关 ====================

    /** 分类树缓存键 */
    public static final String CATEGORY_TREE_KEY = "category:tree";

    // ==================== 购物车缓存相关 ====================

    /** 购物车缓存 Key 前缀，完整格式：cart:user:{userId} */
    public static final String CART_KEY_PREFIX = "cart:user:";

    /** 购物车缓存 TTL（天），默认 7 天 */
    public static final long CART_TTL_DAYS = 7;

    // ==================== 地址缓存相关 ====================

    /** 地址列表缓存 Key 前缀，完整格式：address:list:{userId} */
    public static final String ADDRESS_LIST_CACHE_KEY = "address:list:";

    /** 地址缓存 TTL（小时），默认 24 小时 */
    public static final long ADDRESS_CACHE_TTL_HOURS = 24;

    // ==================== SKU 缓存相关 ====================

    /** SKU 缓存 Key 前缀，完整格式：sku:spu:{spuId}:{suffix} */
    public static final String SKU_CACHE_PREFIX = "sku:spu:";

    /** SKU 公开视角缓存后缀 */
    public static final String SKU_CACHE_PUBLIC_SUFFIX = ":public";

    /** SKU 商家视角缓存后缀 */
    public static final String SKU_CACHE_STORE_SUFFIX = ":store";

    /** SKU 管理视角缓存后缀 */
    public static final String SKU_CACHE_ADMIN_SUFFIX = ":admin";

    /** SKU 公开缓存 TTL（分钟），默认 10 分钟 */
    public static final long SKU_CACHE_TTL_PUBLIC = 10;

    /** SKU 商家/管理缓存 TTL（分钟），默认 5 分钟 */
    public static final long SKU_CACHE_TTL_MGMT = 5;

    // ==================== 库存缓存相关 ====================

    /** SKU 库存 Redis Key 前缀，完整格式：sku:stock:{skuId} */
    public static final String STOCK_PREFIX = "sku:stock:";

    /** SKU 冻结库存 Redis Key 前缀，完整格式：sku:frozen:{skuId} */
    public static final String FROZEN_PREFIX = "sku:frozen:";

    // ==================== 品牌缓存相关 ====================

    /** 品牌 ID 缓存键前缀，完整格式：brand:id:{id} */
    public static final String BRAND_ID_PREFIX = "brand:id:";

    /** 品牌排序缓存键 */
    public static final String BRAND_SORT_KEY = "brand:sort";

    /** 品牌缓存 TTL（分钟），默认 30 分钟 */
    public static final long BRAND_CACHE_TTL_MINUTES = 30;

    // ==================== 店铺缓存相关 ====================

    /** 店铺实体缓存 Key 前缀，完整格式：store:{id} */
    public static final String STORE_CACHE_KEY = "store:";

    /** 店铺详情 VO 缓存 Key 前缀，完整格式：store:detail:{id} */
    public static final String STORE_DETAIL_KEY = "store:detail:";

    /** 商家 ID → 店铺映射缓存 Key 前缀，完整格式：store:seller:{sellerId} */
    public static final String STORE_SELLER_KEY = "store:seller:";

    /** 店铺缓存 TTL（小时），默认 1 小时 */
    public static final long STORE_CACHE_TTL_HOURS = 1;

    // ==================== 仪表盘缓存相关 ====================

    /** 仪表盘缓存 TTL（分钟），默认 5 分钟 */
    public static final long DASHBOARD_CACHE_TTL_MINUTES = 5;
}