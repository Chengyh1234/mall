package com.cyh.mallproduct.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.utils.RedisUtils;
import com.cyh.mallproduct.entity.Brand;
import com.cyh.mallproduct.service.BrandCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 品牌缓存服务实现类
 * <p>
 * 缓存策略：
 * - brand:id:{id} → Brand 实体，TTL 30 分钟（Cache-Aside 模式）
 * - brand:sort    → 全量排序品牌列表，TTL 30 分钟
 * - 所有 set 操作均设置 TTL，避免永不过期导致脏数据残留
 * - 缓存清除使用精确 Key 删除，禁止 KEYS * 命令
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrandCacheServiceImpl implements BrandCacheService {

    private final RedisUtils redisUtils;

    @Override
    public Brand getBrandById(Long id) {
        String cacheKey = RedisConstants.BRAND_ID_PREFIX + id;
        String json = redisUtils.get(cacheKey);
        if (json != null) {
            log.debug("从缓存获取品牌成功，缓存键: {}", cacheKey);
            return redisUtils.fromJson(json, Brand.class);
        }
        return null;
    }

    @Override
    public void setBrandById(Brand brand) {
        String cacheKey = RedisConstants.BRAND_ID_PREFIX + brand.getId();
        redisUtils.setObject(cacheKey, brand, RedisConstants.BRAND_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("设置品牌缓存成功，缓存键: {}, TTL: {} 分钟", cacheKey, RedisConstants.BRAND_CACHE_TTL_MINUTES);
    }

    @Override
    public List<Brand> getBrandsBySort() {
        String json = redisUtils.get(RedisConstants.BRAND_SORT_KEY);
        if (json != null) {
            log.debug("从缓存获取排序品牌列表成功，缓存键: {}", RedisConstants.BRAND_SORT_KEY);
            return redisUtils.fromJson(json, new TypeReference<List<Brand>>() {});
        }
        return null;
    }

    @Override
    public void setBrandsBySort(List<Brand> brands) {
        redisUtils.setObject(RedisConstants.BRAND_SORT_KEY, brands,
                RedisConstants.BRAND_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("设置排序品牌列表缓存成功，缓存键: {}, 品牌数量: {}, TTL: {} 分钟",
                RedisConstants.BRAND_SORT_KEY, brands.size(), RedisConstants.BRAND_CACHE_TTL_MINUTES);
    }

    @Override
    public void clearBrandIdCache(Long id) {
        String cacheKey = RedisConstants.BRAND_ID_PREFIX + id;
        redisUtils.delete(cacheKey);
        log.debug("清除品牌缓存成功，缓存键: {}", cacheKey);
    }
}