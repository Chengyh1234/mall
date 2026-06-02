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
     */
    public static final String USER_CURRENT_SESSION_PREFIX = "user:current_session:";

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

    // ==================== 文件上传常量 ====================

    /**
     * 允许的图片类型
     */
    public static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    };

    /**
     * 默认上传基础路径
     */
    public static final String DEFAULT_UPLOAD_PATH = "./uploads";

    /**
     * 默认图片上传路径
     */
    public static final String DEFAULT_IMAGES_PATH = "./uploads/images";

    /**
     * 默认Logo上传路径
     */
    public static final String DEFAULT_LOGO_PATH = "./uploads/images/brands";

    /**
     * 商品主图上传路径
     */
    public static final String SPU_IMAGE_PATH = "./uploads/images/spu";

    /**
     * SKU规格图上传路径
     */
    public static final String SKU_IMAGE_PATH = "./uploads/images/sku";

    /**
     * 品牌图片上传路径
     */
    public static final String BRANDS_IMAGE_PATH = "./uploads/images/brands";

    /**
     * 店铺图片上传路径
     */
    public static final String STORES_IMAGE_PATH = "./uploads/images/stores";

    /**
     * 用户头像上传路径
     */
    public static final String AVATARS_IMAGE_PATH = "./uploads/images/avatars";

    /**
     * 横幅广告上传路径
     */
    public static final String BANNERS_IMAGE_PATH = "./uploads/images/banners";

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
}
