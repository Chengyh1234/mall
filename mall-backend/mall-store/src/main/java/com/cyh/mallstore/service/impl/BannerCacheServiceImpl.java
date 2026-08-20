package com.cyh.mallstore.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.utils.RedisUtils;
import com.cyh.mallstore.entity.Banner;
import com.cyh.mallstore.service.BannerCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 轮播图缓存服务实现类
 * 基于 Redis 缓存活跃轮播图列表，减少数据库查询压力
 * 数据变更时通过 clearAllBannerCache 清除所有轮播图缓存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerCacheServiceImpl implements BannerCacheService {

    private final RedisUtils redisUtils;

    /**
     * 从 Redis 读取活跃轮播图列表，反序列化失败时返回 null 触发查库
     */
    @Override
    public List<Banner> getActiveBanners() {
        return redisUtils.getObject(RedisConstants.BANNER_ACTIVE_KEY, new TypeReference<List<Banner>>() {});
    }

    /**
     * 将活跃轮播图列表写入 Redis（永久缓存，由数据变更时主动清除）
     */
    @Override
    public void setActiveBanners(List<Banner> banners) {
        redisUtils.setObject(RedisConstants.BANNER_ACTIVE_KEY, banners);
        log.info("设置轮播图列表缓存成功（永久）");
    }

    /**
     * 清除所有轮播图相关缓存（匹配 banner:* 模式），在数据变更时调用
     */
    @Override
    public void clearAllBannerCache() {
        Set<String> keys = redisUtils.getTemplate().keys("banner:*");
        if (keys != null && !keys.isEmpty()) {
            redisUtils.delete(keys);
            log.info("清除所有轮播图缓存成功，共清除 {} 个缓存", keys.size());
        }
    }
}