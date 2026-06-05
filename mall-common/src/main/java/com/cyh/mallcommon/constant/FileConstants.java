package com.cyh.mallcommon.constant;

import java.util.List;

/**
 * 文件上传相关常量类
 * <p>
 * 统一管理文件上传基础路径、子目录路径及允许的图片类型
 * <ul>
 *   <li>SPU 商品图片：./uploads/images/spu</li>
 *   <li>SKU 规格图片：./uploads/images/sku</li>
 *   <li>品牌 Logo：./uploads/images/brand/logo</li>
 *   <li>用户头像：./uploads/images/user/avatars</li>
 *   <li>商家 Logo：./uploads/images/stores/logo</li>
 *   <li>商家横幅：./uploads/images/stores/banner</li>
 *   <li>商家其他图片：./uploads/images/stores</li>
 *   <li>轮播图：./uploads/images/banners</li>
 *   <li>分类图标：./uploads/images/icons</li>
 * </ul>
 *
 * @author cyh
 * @since 2026-06-11
 */
public class FileConstants {

    private FileConstants() {
        // 静态常量类，禁止实例化
    }

    /** 文件上传基础路径 */
    public static final String BASE_PATH = "./uploads";

    /** 文件访问 URL 前缀 */
    public static final String URL_PREFIX = "/uploads";

    /** 允许的图片 MIME 类型 */
    public static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    /** SPU 商品图片 */
    public static final String SPU = "/images/spu";

    /** SKU 规格图片 */
    public static final String SKU = "/images/sku";

    /** 品牌 Logo */
    public static final String BRAND_LOGO = "/images/brand/logo";

    /** 用户头像 */
    public static final String USER_AVATARS = "/images/user/avatars";

    /** 商家 Logo */
    public static final String STORE_LOGO = "/images/stores/logo";

    /** 商家店铺横幅 */
    public static final String STORE_BANNER = "/images/stores/banners";

    /** 商家其他图片 */
    public static final String STORE_IMAGES = "/images/stores";

    /** 轮播图 */
    public static final String BANNERS = "/images/banners";

    /** 分类图标 */
    public static final String CATEGORY_ICONS = "/images/icons";
}