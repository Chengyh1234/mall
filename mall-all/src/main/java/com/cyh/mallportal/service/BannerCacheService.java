package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.Banner;

import java.util.List;

/**
 * 轮播图缓存服务接口
 */
public interface BannerCacheService {

    /**
     * 从缓存获取启用的轮播图列表
     */
    List<Banner> getActiveBanners();

    /**
     * 设置启用的轮播图列表到缓存
     */
    void setActiveBanners(List<Banner> banners);

    /**
     * 清除所有轮播图缓存
     */
    void clearAllBannerCache();
}