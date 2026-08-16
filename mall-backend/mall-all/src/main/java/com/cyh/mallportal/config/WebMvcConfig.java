package com.cyh.mallportal.config;

import com.cyh.mallcommon.constant.FileConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-path:./uploads}")
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // SPU 商品图片
        addHandler(registry, FileConstants.SPU);
        // SKU 规格图片
        addHandler(registry, FileConstants.SKU);
        // 品牌 Logo
        addHandler(registry, FileConstants.BRAND_LOGO);
        // 店铺图片（含 logo/banners/其他）
        addHandler(registry, FileConstants.STORE_IMAGES);
        // 用户头像
        addHandler(registry, FileConstants.USER_AVATARS);
        // 轮播图
        addHandler(registry, FileConstants.BANNERS);
        // 分类图标
        addHandler(registry, FileConstants.CATEGORY_ICONS);
    }

    /**
     * 注册静态资源映射：URL /uploads/subDir/** → 本地文件 basePath + subDir
     */
    private void addHandler(ResourceHandlerRegistry registry, String subDir) {
        // 使用拼接路径避免 subDir 以 / 开头时被当作绝对路径（Linux/Mac 兼容）
        String location = new File(basePath + subDir).toURI().toString();
        registry.addResourceHandler(FileConstants.URL_PREFIX + subDir + "/**")
                .addResourceLocations(location);
    }
}