package com.cyh.mallportal.service.impl;

import com.cyh.mallcommon.constant.MyConstants;
import com.cyh.mallportal.entity.Spu;
import com.cyh.mallportal.service.SpuCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SPU缓存服务实现类
 * 
 * <p>该类负责管理SPU商品数据的Redis缓存，主要用于优化商品列表查询性能。
 * 通过将频繁访问的商品列表数据缓存到Redis中，可以显著减少数据库查询次数，
 * 提升系统响应速度。</p>
 * 
 * <p>缓存策略：
 * <ul>
 *   <li>缓存过期时间：30分钟</li>
 *   <li>缓存键格式：spu:page:c:{categoryId}:b:{brandId}:k:{keywordHash}:s:{status}:p:{page}:ps:{pageSize}</li>
 *   <li>缓存清除时机：商品增删改操作时自动清除所有SPU缓存</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpuCacheServiceImpl implements SpuCacheService {

    /**
     * Redis模板，用于操作Redis缓存
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * JSON序列化工具，用于将商品列表转换为JSON字符串存储到Redis
     */
    private final ObjectMapper objectMapper;

    /**
     * 根据缓存键获取缓存的商品列表
     * 
     * <p>从Redis中读取JSON格式的商品列表数据，反序列化为List&lt;Spu&gt;对象。
     * 如果缓存不存在或反序列化失败，返回null。</p>
     * 
     * @param cacheKey 缓存键
     * @return 商品列表，如果缓存不存在或反序列化失败返回null
     */
    @Override
    public List<Spu> getSpuList(String cacheKey) {
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取SPU列表成功，缓存键: {}", cacheKey);
                return objectMapper.readValue(json, new TypeReference<List<Spu>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化SPU列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    /**
     * 将商品列表写入缓存
     * 
     * <p>将商品列表序列化为JSON字符串后存储到Redis，并设置过期时间。
     * 如果序列化失败，会记录日志但不抛出异常。</p>
     * 
     * @param cacheKey 缓存键
     * @param spuList  商品列表
     */
    @Override
    public void setSpuList(String cacheKey, List<Spu> spuList) {
        try {
            String json = objectMapper.writeValueAsString(spuList);
            redisTemplate.opsForValue().set(cacheKey, json, MyConstants.SPU_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.debug("设置SPU列表缓存成功，缓存键: {}, 商品数量: {}", cacheKey, spuList.size());
        } catch (JsonProcessingException e) {
            log.error("序列化SPU列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
    }

    /**
     * 根据缓存键获取缓存的商品总数
     * 
     * <p>从Redis中读取商品总数的字符串表示，转换为Integer返回。
     * 如果缓存不存在，返回null。</p>
     * 
     * @param countKey 总数缓存键
     * @return 商品总数，如果缓存不存在返回null
     */
    @Override
    public Integer getSpuCount(String countKey) {
        String value = redisTemplate.opsForValue().get(countKey);
        if (StringUtils.hasText(value)) {
            log.debug("从缓存获取SPU总数成功，缓存键: {}, 总数: {}", countKey, value);
            return Integer.parseInt(value);
        }
        return null;
    }

    /**
     * 将商品总数写入缓存
     * 
     * <p>将商品总数转换为字符串后存储到Redis，并设置过期时间。</p>
     * 
     * @param countKey 总数缓存键
     * @param count    商品总数
     */
    @Override
    public void setSpuCount(String countKey, int count) {
        redisTemplate.opsForValue().set(countKey, String.valueOf(count), MyConstants.SPU_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.debug("设置SPU总数缓存成功，缓存键: {}, 总数: {}", countKey, count);
    }

    /**
     * 清除所有SPU相关缓存
     * 
     * <p>删除所有以spu:page:和spu:count:为前缀的缓存键。
     * 此方法在商品增删改操作后调用，确保缓存数据与数据库数据一致。</p>
     */
    @Override
    public void clearAllSpuCache() {
        // 清除商品列表缓存
        var keys = redisTemplate.keys(MyConstants.SPU_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("清除所有SPU列表缓存成功，共清除 {} 个缓存", keys.size());
        }
        
        // 清除商品总数缓存
        var countKeys = redisTemplate.keys(MyConstants.SPU_COUNT_PREFIX + "*");
        if (countKeys != null && !countKeys.isEmpty()) {
            redisTemplate.delete(countKeys);
            log.info("清除所有SPU总数缓存成功，共清除 {} 个缓存", countKeys.size());
        }
    }

    /**
     * 清除指定缓存键的缓存
     * 
     * <p>删除指定的单个缓存键。</p>
     * 
     * @param cacheKey 缓存键
     */
    @Override
    public void clearCache(String cacheKey) {
        redisTemplate.delete(cacheKey);
        log.debug("清除指定缓存成功，缓存键: {}", cacheKey);
    }

    /**
     * 生成分页查询缓存键
     * 
     * <p>根据查询条件生成本次查询的唯一缓存键，确保相同条件的查询能命中同一缓存。
     * 缓存键格式：spu:page:c:{categoryId}:b:{brandId}:k:{keywordHash}:s:{status}:p:{page}:ps:{pageSize}</p>
     * 
     * <p>参数说明：
     * <ul>
     *   <li>categoryId - 分类ID，null时用"all"表示所有分类</li>
     *   <li>brandId - 品牌ID，null时用"all"表示所有品牌</li>
     *   <li>keyword - 搜索关键字，使用hashCode避免长字符串作为键</li>
     *   <li>status - 商品状态，null时用"all"表示所有状态</li>
     *   <li>page - 页码</li>
     *   <li>pageSize - 每页数量</li>
     * </ul>
     * </p>
     * 
     * @param categoryId 分类ID（可为null）
     * @param brandId    品牌ID（可为null）
     * @param keyword    搜索关键字（可为null）
     * @param status     状态（可为null）
     * @param page       页码
     * @param pageSize   每页数量
     * @return 缓存键
     */
    @Override
    public String generateCacheKey(Long categoryId, Long brandId, String keyword, Integer status, Integer page, Integer pageSize) {
        StringBuilder sb = new StringBuilder(MyConstants.SPU_CACHE_PREFIX);
        sb.append("c:").append(categoryId == null ? "all" : categoryId);
        sb.append(":b:").append(brandId == null ? "all" : brandId);
        sb.append(":k:").append(StringUtils.hasText(keyword) ? keyword.hashCode() : "none");
        sb.append(":s:").append(status == null ? "all" : status);
        sb.append(":p:").append(page);
        sb.append(":ps:").append(pageSize);
        return sb.toString();
    }

    /**
     * 生成总数缓存键
     * 
     * <p>根据查询条件生成商品总数的缓存键，格式与分页缓存键类似但不含页码信息。
     * 缓存键格式：spu:count:c:{categoryId}:b:{brandId}:k:{keywordHash}:s:{status}</p>
     * 
     * @param categoryId 分类ID（可为null）
     * @param brandId    品牌ID（可为null）
     * @param keyword    搜索关键字（可为null）
     * @param status     状态（可为null）
     * @return 总数缓存键
     */
    @Override
    public String generateCountKey(Long categoryId, Long brandId, String keyword, Integer status) {
        StringBuilder sb = new StringBuilder(MyConstants.SPU_COUNT_PREFIX);
        sb.append("c:").append(categoryId == null ? "all" : categoryId);
        sb.append(":b:").append(brandId == null ? "all" : brandId);
        sb.append(":k:").append(StringUtils.hasText(keyword) ? keyword.hashCode() : "none");
        sb.append(":s:").append(status == null ? "all" : status);
        return sb.toString();
    }
}