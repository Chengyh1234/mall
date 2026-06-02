package com.cyh.mallportal.service.impl;

import com.cyh.mallportal.entity.Brand;
import com.cyh.mallportal.service.BrandCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 品牌缓存服务实现类
 * 
 * <p>该类负责管理品牌数据的Redis缓存，主要用于优化品牌查询性能。
 * 通过将频繁访问的品牌数据缓存到Redis中，可以显著减少数据库查询次数，
 * 提升系统响应速度。</p>
 * 
 * <p>缓存策略：
 * <ul>
 *   <li>缓存永不过期，品牌数据变化时手动清除</li>
 *   <li>缓存键格式：brand:id:{id}、brand:status:{status}、brand:sort、brand:list:{hash}、brand:page:{hash}:p:{page}:ps:{pageSize}</li>
 *   <li>缓存清除时机：品牌增删改操作时自动清除所有品牌缓存</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrandCacheServiceImpl implements BrandCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String BRAND_ID_PREFIX = "brand:id:";
    private static final String BRAND_STATUS_PREFIX = "brand:status:";
    private static final String BRAND_SORT_KEY = "brand:sort";
    private static final String BRAND_LIST_PREFIX = "brand:list:";
    private static final String BRAND_PAGE_PREFIX = "brand:page:";

    @Override
    public Brand getBrandById(Long id) {
        String cacheKey = BRAND_ID_PREFIX + id;
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
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
        String cacheKey = BRAND_ID_PREFIX + brand.getId();
        try {
            String json = objectMapper.writeValueAsString(brand);
            redisTemplate.opsForValue().set(cacheKey, json);
            log.debug("设置品牌缓存成功（永久），缓存键: {}", cacheKey);
        } catch (JsonProcessingException e) {
            log.error("序列化品牌缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
    }

    @Override
    public List<Brand> getBrandsByStatus(Integer status) {
        String cacheKey = BRAND_STATUS_PREFIX + status;
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取状态品牌列表成功，缓存键: {}", cacheKey);
                return objectMapper.readValue(json, new TypeReference<List<Brand>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化状态品牌列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    @Override
    public void setBrandsByStatus(Integer status, List<Brand> brands) {
        String cacheKey = BRAND_STATUS_PREFIX + status;
        try {
            String json = objectMapper.writeValueAsString(brands);
            redisTemplate.opsForValue().set(cacheKey, json);
            log.debug("设置状态品牌列表缓存成功（永久），缓存键: {}, 品牌数量: {}", cacheKey, brands.size());
        } catch (JsonProcessingException e) {
            log.error("序列化状态品牌列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
    }

    @Override
    public List<Brand> getBrandsBySort() {
        try {
            String json = redisTemplate.opsForValue().get(BRAND_SORT_KEY);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取排序品牌列表成功，缓存键: {}", BRAND_SORT_KEY);
                return objectMapper.readValue(json, new TypeReference<List<Brand>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化排序品牌列表缓存失败，缓存键: {}, 异常: {}", BRAND_SORT_KEY, e.getMessage());
        }
        return null;
    }

    @Override
    public void setBrandsBySort(List<Brand> brands) {
        try {
            String json = objectMapper.writeValueAsString(brands);
            redisTemplate.opsForValue().set(BRAND_SORT_KEY, json);
            log.debug("设置排序品牌列表缓存成功（永久），缓存键: {}, 品牌数量: {}", BRAND_SORT_KEY, brands.size());
        } catch (JsonProcessingException e) {
            log.error("序列化排序品牌列表缓存失败，缓存键: {}, 异常: {}", BRAND_SORT_KEY, e.getMessage());
        }
    }

    @Override
    public List<Brand> getBrandList(String cacheKey) {
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取品牌列表成功，缓存键: {}", cacheKey);
                return objectMapper.readValue(json, new TypeReference<List<Brand>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化品牌列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    @Override
    public void setBrandList(String cacheKey, List<Brand> brands) {
        try {
            String json = objectMapper.writeValueAsString(brands);
            redisTemplate.opsForValue().set(cacheKey, json);
            log.debug("设置品牌列表缓存成功（永久），缓存键: {}, 品牌数量: {}", cacheKey, brands.size());
        } catch (JsonProcessingException e) {
            log.error("序列化品牌列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
    }

    @Override
    public void clearAllBrandCache() {
        var idKeys = redisTemplate.keys(BRAND_ID_PREFIX + "*");
        if (idKeys != null && !idKeys.isEmpty()) {
            redisTemplate.delete(idKeys);
            log.info("清除所有品牌详情缓存成功，共清除 {} 个缓存", idKeys.size());
        }

        var statusKeys = redisTemplate.keys(BRAND_STATUS_PREFIX + "*");
        if (statusKeys != null && !statusKeys.isEmpty()) {
            redisTemplate.delete(statusKeys);
            log.info("清除所有品牌状态缓存成功，共清除 {} 个缓存", statusKeys.size());
        }

        redisTemplate.delete(BRAND_SORT_KEY);
        log.info("清除品牌排序缓存成功");

        var listKeys = redisTemplate.keys(BRAND_LIST_PREFIX + "*");
        if (listKeys != null && !listKeys.isEmpty()) {
            redisTemplate.delete(listKeys);
            log.info("清除所有品牌列表缓存成功，共清除 {} 个缓存", listKeys.size());
        }

        var pageKeys = redisTemplate.keys(BRAND_PAGE_PREFIX + "*");
        if (pageKeys != null && !pageKeys.isEmpty()) {
            redisTemplate.delete(pageKeys);
            log.info("清除所有品牌分页缓存成功，共清除 {} 个缓存", pageKeys.size());
        }
    }

    @Override
    public String generatePageCacheKey(Brand brand, Integer page, Integer pageSize) {
        StringBuilder sb = new StringBuilder(BRAND_PAGE_PREFIX);
        if (brand != null) {
            sb.append("id:").append(brand.getId() == null ? "all" : brand.getId());
            sb.append(":name:").append(StringUtils.hasText(brand.getName()) ? brand.getName().hashCode() : "none");
            sb.append(":status:").append(brand.getStatus() == null ? "all" : brand.getStatus());
        } else {
            sb.append("id:all:name:none:status:all");
        }
        sb.append(":p:").append(page).append(":ps:").append(pageSize);
        return sb.toString();
    }

    @Override
    public String generatePageCountKey(Brand brand) {
        StringBuilder sb = new StringBuilder(BRAND_PAGE_PREFIX).append("total:");
        if (brand != null) {
            sb.append("id:").append(brand.getId() == null ? "all" : brand.getId());
            sb.append(":name:").append(StringUtils.hasText(brand.getName()) ? brand.getName().hashCode() : "none");
            sb.append(":status:").append(brand.getStatus() == null ? "all" : brand.getStatus());
        } else {
            sb.append("id:all:name:none:status:all");
        }
        return sb.toString();
    }

    @Override
    public String generateListCacheKey(Brand brand) {
        StringBuilder sb = new StringBuilder(BRAND_LIST_PREFIX);
        if (brand != null) {
            sb.append("id:").append(brand.getId() == null ? "all" : brand.getId());
            sb.append(":name:").append(StringUtils.hasText(brand.getName()) ? brand.getName().hashCode() : "none");
            sb.append(":status:").append(brand.getStatus() == null ? "all" : brand.getStatus());
        } else {
            sb.append("id:all:name:none:status:all");
        }
        return sb.toString();
    }

    @Override
    public Integer getBrandCount(String countKey) {
        String value = redisTemplate.opsForValue().get(countKey);
        if (StringUtils.hasText(value)) {
            log.debug("从缓存获取品牌总数成功，缓存键: {}, 总数: {}", countKey, value);
            return Integer.parseInt(value);
        }
        return null;
    }

    @Override
    public void setBrandCount(String countKey, int count) {
        redisTemplate.opsForValue().set(countKey, String.valueOf(count));
        log.debug("设置品牌总数缓存成功（永久），缓存键: {}, 总数: {}", countKey, count);
    }
}