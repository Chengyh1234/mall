package com.cyh.mallportal.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallportal.service.SpuCacheService;
import com.cyh.mallportal.vo.SpuAdminDetailVo;
import com.cyh.mallportal.vo.SpuDetailVo;
import com.cyh.mallportal.vo.SpuSellerDetailVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SPU 详情缓存服务实现
 *
 * <p>存储方式：将 VO 对象序列化为 JSON 字符串存入 Redis，读取时反序列化回 VO。
 * 使用 StringRedisTemplate 而非 RedisTemplate，由调用方通过 ObjectMapper 显式控制序列化，
 * 避免 Jackson 默认类型反序列化问题。</p>
 *
 * <p>缓存策略：Cache-Aside 模式</p>
 * <ul>
 *   <li>读操作：先查缓存 → 命中直接返回 → 未命中查 DB → 回写缓存 → 返回</li>
 *   <li>写操作：更新 DB → 清除该 SPU 的全部视角缓存</li>
 *   <li>三种视角独立缓存 Key，互不干扰</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpuCacheServiceImpl implements SpuCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    /**
     * 获取公开 SPU 详情缓存
     * <p>缓存 Key：spu:detail:{spuId}，TTL：30 分钟</p>
     */
    @Override
    public SpuDetailVo getSpuDetail(Long spuId) {
        String key = RedisConstants.SPU_DETAIL_PREFIX + spuId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, SpuDetailVo.class);
        } catch (JsonProcessingException e) {
            log.warn("反序列化公开SPU详情缓存失败: spuId={}", spuId, e);
            return null;
        }
    }

    /**
     * 写入公开 SPU 详情缓存（序列化为 JSON 字符串存储）
     */
    @Override
    public void setSpuDetail(Long spuId, SpuDetailVo vo) {
        String key = RedisConstants.SPU_DETAIL_PREFIX + spuId;
        try {
            String json = objectMapper.writeValueAsString(vo);
            stringRedisTemplate.opsForValue().set(key, json, RedisConstants.SPU_DETAIL_CACHE_TTL_PUBLIC, TimeUnit.MINUTES);
            log.debug("写入公开SPU详情缓存: spuId={}", spuId);
        } catch (JsonProcessingException e) {
            log.warn("序列化公开SPU详情缓存失败: spuId={}", spuId, e);
        }
    }

    /**
     * 获取商家端 SPU 详情缓存
     * <p>缓存 Key：spu:detail:seller:{spuId}，TTL：10 分钟</p>
     */
    @Override
    public SpuSellerDetailVo getSpuSellerDetail(Long spuId) {
        String key = RedisConstants.SPU_DETAIL_SELLER_PREFIX + spuId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, SpuSellerDetailVo.class);
        } catch (JsonProcessingException e) {
            log.warn("反序列化商家SPU详情缓存失败: spuId={}", spuId, e);
            return null;
        }
    }

    /**
     * 写入商家端 SPU 详情缓存（序列化为 JSON 字符串存储）
     */
    @Override
    public void setSpuSellerDetail(Long spuId, SpuSellerDetailVo vo) {
        String key = RedisConstants.SPU_DETAIL_SELLER_PREFIX + spuId;
        try {
            String json = objectMapper.writeValueAsString(vo);
            stringRedisTemplate.opsForValue().set(key, json, RedisConstants.SPU_DETAIL_CACHE_TTL_MGMT, TimeUnit.MINUTES);
            log.debug("写入商家SPU详情缓存: spuId={}", spuId);
        } catch (JsonProcessingException e) {
            log.warn("序列化商家SPU详情缓存失败: spuId={}", spuId, e);
        }
    }

    /**
     * 获取管理端 SPU 详情缓存
     * <p>缓存 Key：spu:detail:admin:{spuId}，TTL：10 分钟</p>
     */
    @Override
    public SpuAdminDetailVo getSpuAdminDetail(Long spuId) {
        String key = RedisConstants.SPU_DETAIL_ADMIN_PREFIX + spuId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, SpuAdminDetailVo.class);
        } catch (JsonProcessingException e) {
            log.warn("反序列化管理SPU详情缓存失败: spuId={}", spuId, e);
            return null;
        }
    }

    /**
     * 写入管理端 SPU 详情缓存（序列化为 JSON 字符串存储）
     */
    @Override
    public void setSpuAdminDetail(Long spuId, SpuAdminDetailVo vo) {
        String key = RedisConstants.SPU_DETAIL_ADMIN_PREFIX + spuId;
        try {
            String json = objectMapper.writeValueAsString(vo);
            stringRedisTemplate.opsForValue().set(key, json, RedisConstants.SPU_DETAIL_CACHE_TTL_MGMT, TimeUnit.MINUTES);
            log.debug("写入管理SPU详情缓存: spuId={}", spuId);
        } catch (JsonProcessingException e) {
            log.warn("序列化管理SPU详情缓存失败: spuId={}", spuId, e);
        }
    }

    /**
     * 清除指定 SPU 的全部视角详情缓存
     *
     * <p>使用精确 Key 删除，避免 {@code KEYS *} 阻塞命令。
     * 三种视角（公开/商家/管理）的缓存一次性全部清除，
     * 确保下次读取时能从 DB 拿到最新数据。</p>
     *
     * @param spuId SPU ID
     */
    @Override
    public void clearSpuDetailCache(Long spuId) {
        String publicKey = RedisConstants.SPU_DETAIL_PREFIX + spuId;
        String sellerKey = RedisConstants.SPU_DETAIL_SELLER_PREFIX + spuId;
        String adminKey = RedisConstants.SPU_DETAIL_ADMIN_PREFIX + spuId;
        stringRedisTemplate.delete(List.of(publicKey, sellerKey, adminKey));
        log.debug("清除SPU详情缓存: spuId={}", spuId);
    }
}