package com.cyh.mallportal.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallportal.entity.Brand;
import com.cyh.mallportal.service.BrandCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Brand getBrandById(Long id) {
        String cacheKey = RedisConstants.BRAND_ID_PREFIX + id;
        try {
            String json = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取品牌成功，缓存键: {}", cacheKey);
                return objectMapper.readValue(json, Brand.class);
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化品牌缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    @Override
    public void setBrandById(Brand brand) {
        String cacheKey = RedisConstants.BRAND_ID_PREFIX + brand.getId();
        try {
            String json = objectMapper.writeValueAsString(brand);
            stringRedisTemplate.opsForValue().set(cacheKey, json,
                    RedisConstants.BRAND_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("设置品牌缓存成功，缓存键: {}, TTL: {} 分钟", cacheKey, RedisConstants.BRAND_CACHE_TTL_MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化品牌缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
    }

    @Override
    public List<Brand> getBrandsBySort() {
        try {
            String json = stringRedisTemplate.opsForValue().get(RedisConstants.BRAND_SORT_KEY);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取排序品牌列表成功，缓存键: {}", RedisConstants.BRAND_SORT_KEY);
                return objectMapper.readValue(json, new TypeReference<List<Brand>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化排序品牌列表缓存失败，缓存键: {}, 异常: {}", RedisConstants.BRAND_SORT_KEY, e.getMessage());
        }
        return null;
    }

    @Override
    public void setBrandsBySort(List<Brand> brands) {
        try {
            String json = objectMapper.writeValueAsString(brands);
            stringRedisTemplate.opsForValue().set(RedisConstants.BRAND_SORT_KEY, json,
                    RedisConstants.BRAND_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("设置排序品牌列表缓存成功，缓存键: {}, 品牌数量: {}, TTL: {} 分钟",
                    RedisConstants.BRAND_SORT_KEY, brands.size(), RedisConstants.BRAND_CACHE_TTL_MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化排序品牌列表缓存失败，异常: {}", e.getMessage());
        }
    }

    @Override
    public void clearBrandIdCache(Long id) {
        String cacheKey = RedisConstants.BRAND_ID_PREFIX + id;
        stringRedisTemplate.delete(cacheKey);
        log.debug("清除品牌缓存成功，缓存键: {}", cacheKey);
    }
}