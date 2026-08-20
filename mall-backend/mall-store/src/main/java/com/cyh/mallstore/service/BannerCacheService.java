package com.cyh.mallstore.service;

import com.cyh.mallstore.entity.Banner;

import java.util.List;

/**
 * 轮播图缓存服务接口
 * 定义活跃轮播图列表的缓存读取、写入和清除操作
 */
public interface BannerCacheService {

    List<Banner> getActiveBanners();

    void setActiveBanners(List<Banner> banners);

    void clearAllBannerCache();
}