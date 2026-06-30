package com.cyh.mallportal.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallportal.entity.Banner;
import com.cyh.mallportal.service.BannerCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * 轮播图缓存服务实现类
 *
 * <p>轮播图属于低频修改、高频读取的数据，
 * 采用永久缓存策略，数据变更时手动清除缓存。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerCacheServiceImpl implements BannerCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<Banner> getActiveBanners() {
        try {
            String json = stringRedisTemplate.opsForValue().get(RedisConstants.BANNER_ACTIVE_KEY);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取启用的轮播图列表成功，缓存键: {}", RedisConstants.BANNER_ACTIVE_KEY);
                return objectMapper.readValue(json, new TypeReference<List<Banner>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化轮播图列表缓存失败，缓存键: {}, 异常: {}", RedisConstants.BANNER_ACTIVE_KEY, e.getMessage());
        }
        return null;
    }

    @Override
    public void setActiveBanners(List<Banner> banners) {
        try {
            String json = objectMapper.writeValueAsString(banners);
            stringRedisTemplate.opsForValue().set(RedisConstants.BANNER_ACTIVE_KEY, json);
            log.debug("设置轮播图列表缓存成功（永久），缓存键: {}, 轮播图数量: {}", RedisConstants.BANNER_ACTIVE_KEY, banners.size());
        } catch (JsonProcessingException e) {
            log.error("序列化轮播图列表缓存失败，缓存键: {}, 异常: {}", RedisConstants.BANNER_ACTIVE_KEY, e.getMessage());
        }
    }

    @Override
    public void clearAllBannerCache() {
        Set<String> keys = stringRedisTemplate.keys("banner:*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
            log.info("清除所有轮播图缓存成功，共清除 {} 个缓存", keys.size());
        }
    }
}