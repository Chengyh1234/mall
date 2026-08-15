package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallportal.entity.*;
import com.cyh.mallportal.mapper.*;
import com.cyh.mallportal.mq.event.CacheDomain;
import com.cyh.mallportal.mq.event.CacheInvalidateEvent;
import com.cyh.mallportal.mq.publisher.CacheEventPublisher;
import com.cyh.mallportal.service.SkuService;
import com.cyh.mallportal.service.SpuService;
import com.cyh.mallportal.vo.SkuAdminVo;
import com.cyh.mallportal.vo.SkuStoreVo;
import com.cyh.mallportal.vo.SkuVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.cyh.mallcommon.constant.RedisConstants;
import java.util.stream.Collectors;

/**
 * SKU服务实现类
 * 提供商品库存单元业务逻辑的具体实现
 * <p>
 * 缓存设计说明：
 * - 缓存 Key: sku:spu:{spuId}:public/store/admin 分别对应三种视角的SKU列表
 * - 缓存 TTL: 10分钟（公开）/ 5分钟（商家/管理）
 * - inStock 不从缓存读取，改为实时查询 Redis 库存 Key（sku:stock:{skuId}），确保库存状态实时准确
 * - 所有 SKU 增、删、改、上架下架、库存变更操作均主动清除缓存，保证数据一致性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkuServiceImpl implements SkuService {

    private final SkuMapper skuMapper;
    private final AttributeMapper attributeMapper;
    private final AttributeValueMapper attributeValueMapper;
    private final SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    private final SpuService spuService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final CacheEventPublisher cacheEventPublisher;

    // ==================== 带缓存的查询方法 ====================

    /**
     * 根据SPU ID获取公开SKU列表（包含销售属性）
     * 命中缓存时直接从Redis返回，inStock实时从Redis库存Key查询
     */
    @Override
    public List<SkuVo> getBySpuIdWithAttributes(Long spuId) {
        String key = RedisConstants.SKU_CACHE_PREFIX + spuId + RedisConstants.SKU_CACHE_PUBLIC_SUFFIX;

        // Step1: 读缓存
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                List<SkuVo> result = objectMapper.readValue(cached,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, SkuVo.class));
                // 实时注入 inStock（从 Redis 库存 Key 查询）
                fillInStock(result);
                return result;
            } catch (JsonProcessingException e) {
                log.error("反序列化SKU缓存失败, key: {}", key, e);
                stringRedisTemplate.delete(key);
            }
        }

        // Step2: 缓存未命中，查库组装
        List<SkuVo> result = doGetBySpuIdWithAttributes(spuId);
        if (result == null) {
            return new ArrayList<>();
        }

        // Step3: 写缓存（inStock不缓存，写为false）
        result.forEach(vo -> vo.setInStock(false));
        cacheResult(key, result, RedisConstants.SKU_CACHE_TTL_PUBLIC);

        // Step4: 实时查询库存
        fillInStock(result);

        return result;
    }

    /**
     * 商家端：根据SPU ID获取SKU列表（包含销售属性）
     * 命中缓存时直接从Redis返回
     */
    @Override
    public List<SkuStoreVo> getStoreBySpuIdWithAttributes(Long spuId) {
        String key = RedisConstants.SKU_CACHE_PREFIX + spuId + RedisConstants.SKU_CACHE_STORE_SUFFIX;

        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, SkuStoreVo.class));
            } catch (JsonProcessingException e) {
                log.error("反序列化商家SKU缓存失败, key: {}", key, e);
                stringRedisTemplate.delete(key);
            }
        }

        List<SkuStoreVo> result = doGetStoreBySpuIdWithAttributes(spuId);
        if (result == null) {
            return new ArrayList<>();
        }

        //result.forEach(vo -> {
        //    vo.setInStock(false);
        //    vo.setStock(null);
        //});
        cacheResult(key, result, RedisConstants.SKU_CACHE_TTL_MGMT);

        return result;
    }

    /**
     * 管理员端：根据SPU ID获取SKU列表（包含销售属性）
     * 命中缓存时直接从Redis返回
     */
    @Override
    public List<SkuAdminVo> getAdminBySpuIdWithAttributes(Long spuId) {
        String key = RedisConstants.SKU_CACHE_PREFIX + spuId + RedisConstants.SKU_CACHE_ADMIN_SUFFIX;

        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, SkuAdminVo.class));
            } catch (JsonProcessingException e) {
                log.error("反序列化管理SKU缓存失败, key: {}", key, e);
                stringRedisTemplate.delete(key);
            }
        }

        List<SkuAdminVo> result = doGetAdminBySpuIdWithAttributes(spuId);
        if (result == null) {
            return new ArrayList<>();
        }

        //result.forEach(vo -> {
        //    vo.setInStock(false);
        //    vo.setStock(null);
        //    vo.setFrozenStock(null);
        //});
        cacheResult(key, result, RedisConstants.SKU_CACHE_TTL_MGMT);

        return result;
    }

    // ==================== 缓存清除方法 ====================

    /**
     * 事务提交后，异步发布 SKU 缓存失效事件
     */
    private void publishSkuCacheInvalidate(Long spuId) {
        if (spuId == null) {
            return;
        }
        String prefix = RedisConstants.SKU_CACHE_PREFIX + spuId;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                        .setDomain(CacheDomain.SKU)
                        .setExactKeys(List.of(
                                prefix + RedisConstants.SKU_CACHE_PUBLIC_SUFFIX,
                                prefix + RedisConstants.SKU_CACHE_STORE_SUFFIX,
                                prefix + RedisConstants.SKU_CACHE_ADMIN_SUFFIX
                        )));
            }
        });
        log.debug("发布SKU缓存失效事件, spuId: {}", spuId);
    }

    // ==================== 非缓存查询方法（供缓存回源使用） ====================

    /**
     * 实际查库：获取公开SKU列表（不含缓存逻辑）
     */
    private List<SkuVo> doGetBySpuIdWithAttributes(Long spuId) {
        List<Sku> skus = getBySpuId(spuId);
        if (skus == null || skus.isEmpty()) {
            return new ArrayList<>();
        }
        return skus.stream()
                .filter(sku -> sku.getStatus() != null && sku.getStatus() == 1)
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }

    /**
     * 实际查库：获取商家SKU列表（不含缓存逻辑）
     */
    private List<SkuStoreVo> doGetStoreBySpuIdWithAttributes(Long spuId) {
        List<Sku> skus = getBySpuId(spuId);
        if (skus == null || skus.isEmpty()) {
            return new ArrayList<>();
        }
        return skus.stream()
                .map(this::convertToStoreVo)
                .collect(Collectors.toList());
    }

    /**
     * 实际查库：获取管理员SKU列表（不含缓存逻辑）
     */
    private List<SkuAdminVo> doGetAdminBySpuIdWithAttributes(Long spuId) {
        List<Sku> skus = getBySpuId(spuId);
        if (skus == null || skus.isEmpty()) {
            return new ArrayList<>();
        }
        return skus.stream()
                .map(this::convertToAdminVo)
                .collect(Collectors.toList());
    }

    /**
     * 实时填充 inStock：从 Redis 计算实时可售库存（stock - frozen）
     */
    private void fillInStock(List<SkuVo> skus) {
        if (skus == null || skus.isEmpty()) {
            return;
        }
        for (SkuVo vo : skus) {
            String stockKey = "sku:stock:" + vo.getId();
            String frozenKey = "sku:frozen:" + vo.getId();
            String stockVal = stringRedisTemplate.opsForValue().get(stockKey);
            String frozenVal = stringRedisTemplate.opsForValue().get(frozenKey);
            try {
                int stock = stockVal != null ? Integer.parseInt(stockVal) : 0;
                int frozen = frozenVal != null ? Integer.parseInt(frozenVal) : 0;
                vo.setInStock((stock - frozen) > 0);
            } catch (NumberFormatException e) {
                vo.setInStock(false);
            }
        }
    }

    /**
     * 写入缓存并设置TTL
     */
    private void cacheResult(String key, Object value, long ttlMinutes) {
        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, ttlMinutes, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化SKU缓存失败, key: {}", key, e);
        }
    }

    // ==================== 原有方法（增删改查） ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        log.info("删除SKU: {}", id);

        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            log.warn("SKU不存在: {}", id);
            return false;
        }

        skuMapper.deleteById(id);

        spuService.updateMinPriceForSpu(sku.getSpuId());

        // 清除缓存
        publishSkuCacheInvalidate(sku.getSpuId());

        log.info("删除SKU成功: {}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBySpuId(Long spuId) {
        log.info("删除SPU下所有SKU, SPU ID: {}", spuId);

        List<Sku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spuId));
        for (Sku sku : skus) {
            skuSaleAttrValueMapper.delete(
                    new LambdaQueryWrapper<SkuSaleAttrValue>().eq(SkuSaleAttrValue::getSkuId, sku.getId()));
        }

        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getSpuId, spuId);
        skuMapper.delete(wrapper);

        spuService.updateMinPriceForSpu(spuId);

        // 清除缓存
        publishSkuCacheInvalidate(spuId);

        log.info("删除SPU下SKU完成并已清理销售属性绑定");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        log.info("批量删除SKU, IDs: {}", ids);

        Map<Long, Long> idToSpuIdMap = new HashMap<>();
        for (Long id : ids) {
            Sku sku = skuMapper.selectById(id);
            if (sku != null) {
                idToSpuIdMap.put(id, sku.getSpuId());
            }
        }

        for (Long id : ids) {
            skuSaleAttrValueMapper.delete(
                    new LambdaQueryWrapper<SkuSaleAttrValue>().eq(SkuSaleAttrValue::getSkuId, id));
        }

        for (Long id : ids) {
            skuMapper.deleteById(id);
        }

        Set<Long> affectedSpuIds = new HashSet<>(idToSpuIdMap.values());
        for (Long spuId : affectedSpuIds) {
            spuService.updateMinPriceForSpu(spuId);
            // 清除缓存
            publishSkuCacheInvalidate(spuId);
        }

        log.info("批量删除SKU完成, 数量: {}", ids.size());
        return ids.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Sku sku) {
        log.info("更新SKU: {}", sku.getId());

        sku.setUpdatedAt(LocalDateTime.now());
        int rows = skuMapper.updateById(sku);

        boolean success = rows > 0;
        if (success) {
            log.info("更新SKU成功: {}", sku.getId());

            Sku updatedSku = skuMapper.selectById(sku.getId());
            if (updatedSku != null) {
                spuService.updateMinPriceForSpu(updatedSku.getSpuId());
                // 清除缓存
                publishSkuCacheInvalidate(updatedSku.getSpuId());
            }
        } else {
            log.warn("更新SKU失败: {}", sku.getId());
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStock(Long id, Integer stock) {
        log.info("更新SKU库存, ID: {}, 库存: {}", id, stock);

        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            log.warn("SKU不存在: {}", id);
            return false;
        }

        sku.setStock(stock);
        sku.setUpdatedAt(LocalDateTime.now());
        int rows = skuMapper.updateById(sku);

        // 清除缓存
        publishSkuCacheInvalidate(sku.getSpuId());

        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Long id, Integer quantity) {
        log.info("扣减SKU库存, ID: {}, 数量: {}", id, quantity);

        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            log.warn("SKU不存在: {}", id);
            return false;
        }

        if (sku.getStock() < quantity) {
            log.warn("库存不足, SKU: {}, 库存: {}, 需要: {}", id, sku.getStock(), quantity);
            return false;
        }

        sku.setStock(sku.getStock() - quantity);
        sku.setUpdatedAt(LocalDateTime.now());
        skuMapper.updateById(sku);

        // 注：decreaseStock 由下单流程调用（高频），不清除缓存以免影响性能
        // inStock 实时性通过 fillInStock() 从 Redis 库存 Key 保证
        // 库存变更已同步到 Redis（InventoryRedisService）

        log.info("扣减库存成功, SKU: {}, 剩余库存: {}", id, sku.getStock());
        return true;
    }

    @Override
    public Sku getById(Long id) {
        return skuMapper.selectById(id);
    }

    @Override
    public List<Sku> getBySpuId(Long spuId) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getSpuId, spuId);
        wrapper.orderByAsc(Sku::getId);
        return skuMapper.selectList(wrapper);
    }

    @Override
    public List<Sku> getAll() {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Sku::getId);
        return skuMapper.selectList(wrapper);
    }

    @Override
    public int count(Long spuId, Integer status) {
        LambdaQueryWrapper<Sku> wrapper = buildWrapper(spuId, status);
        long totalLong = skuMapper.selectCount(wrapper);
        return (int) totalLong;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enable(Long id) {
        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            return false;
        }
        sku.setStatus(1);
        sku.setUpdatedAt(LocalDateTime.now());
        boolean success = skuMapper.updateById(sku) > 0;

        // 清除缓存
        publishSkuCacheInvalidate(sku.getSpuId());

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disable(Long id) {
        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            return false;
        }
        sku.setStatus(0);
        sku.setUpdatedAt(LocalDateTime.now());
        boolean success = skuMapper.updateById(sku) > 0;

        // 清除缓存
        publishSkuCacheInvalidate(sku.getSpuId());

        return success;
    }

    @Override
    public Integer getTotalStock(Long spuId) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getSpuId, spuId);
        wrapper.eq(Sku::getStatus, 1);

        List<Sku> skus = skuMapper.selectList(wrapper);
        return skus.stream()
                .map(Sku::getStock)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    // ==================== VO转换方法 ====================

    private SkuVo convertToVo(Sku sku) {
        SkuVo vo = SkuVo.builder()
                .id(sku.getId())
                .spuId(sku.getSpuId())
                .price(sku.getPrice())
                .marketPrice(sku.getMarketPrice())
                .image(sku.getImage())
                .weight(sku.getWeight())
                .inStock(false) // 不缓存，由 fillInStock 实时查询
                .build();

        List<Map<String, Object>> attributes = getSkuSaleAttributes(sku.getId());
        vo.setSaleAttributes(attributes);

        return vo;
    }

    private SkuStoreVo convertToStoreVo(Sku sku) {
        SkuStoreVo vo = new SkuStoreVo();
        vo.setId(sku.getId());
        vo.setSpuId(sku.getSpuId());
        vo.setPrice(sku.getPrice());
        vo.setMarketPrice(sku.getMarketPrice());
        vo.setImage(sku.getImage());
        vo.setWeight(sku.getWeight());
        vo.setInStock(false);
        // 商家字段不缓存（写为空）
        vo.setCostPrice(sku.getCostPrice());
        vo.setStock(sku.getStock());
        vo.setWarnStock(sku.getWarnStock());
        vo.setStatus(sku.getStatus());
        vo.setCreatedAt(sku.getCreatedAt());
        vo.setUpdatedAt(sku.getUpdatedAt());

        List<Map<String, Object>> attributes = getSkuSaleAttributes(sku.getId());
        vo.setSaleAttributes(attributes);

        return vo;
    }

    private SkuAdminVo convertToAdminVo(Sku sku) {
        SkuAdminVo vo = new SkuAdminVo();
        vo.setId(sku.getId());
        vo.setSpuId(sku.getSpuId());
        vo.setPrice(sku.getPrice());
        vo.setMarketPrice(sku.getMarketPrice());
        vo.setImage(sku.getImage());
        vo.setWeight(sku.getWeight());
        vo.setInStock(false);
        vo.setCostPrice(sku.getCostPrice());
        vo.setStock(sku.getStock());
        vo.setWarnStock(sku.getWarnStock());
        vo.setStatus(sku.getStatus());
        vo.setCreatedAt(sku.getCreatedAt());
        vo.setUpdatedAt(sku.getUpdatedAt());
        vo.setFrozenStock(sku.getFrozenStock());
        vo.setIsDeleted(sku.getIsDeleted());

        List<Map<String, Object>> attributes = getSkuSaleAttributes(sku.getId());
        vo.setSaleAttributes(attributes);

        return vo;
    }

    private List<Map<String, Object>> getSkuSaleAttributes(Long skuId) {
        List<SkuSaleAttrValue> skuAttrValues = skuSaleAttrValueMapper.getBySkuId(skuId);
        if (skuAttrValues == null || skuAttrValues.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> attrValueIds = skuAttrValues.stream()
                .map(SkuSaleAttrValue::getAttrValueId)
                .distinct()
                .collect(Collectors.toList());

        List<AttributeValue> attrValues = attributeValueMapper.selectBatchIds(attrValueIds);
        Map<Long, AttributeValue> attrValueMap = attrValues.stream()
                .collect(Collectors.toMap(AttributeValue::getId, v -> v));

        List<Long> attrIds = attrValues.stream()
                .map(AttributeValue::getAttrId)
                .distinct()
                .collect(Collectors.toList());

        List<Attribute> attributes = attributeMapper.selectBatchIds(attrIds);
        Map<Long, Attribute> attrMap = attributes.stream()
                .collect(Collectors.toMap(Attribute::getId, a -> a));

        return skuAttrValues.stream()
                .map(skuAttrValue -> {
                    AttributeValue attrValue = attrValueMap.get(skuAttrValue.getAttrValueId());
                    if (attrValue == null) return null;

                    Attribute attr = attrMap.get(attrValue.getAttrId());
                    if (attr == null) return null;

                    Map<String, Object> map = new HashMap<>();
                    map.put("attrId", attr.getId());
                    map.put("attrName", attr.getName());
                    map.put("valueId", attrValue.getId());
                    map.put("value", attrValue.getValue());
                    map.put("imageUrl", attrValue.getImageUrl());
                    return map;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private LambdaQueryWrapper<Sku> buildWrapper(Long spuId, Integer status) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        if (spuId != null) {
            wrapper.eq(Sku::getSpuId, spuId);
        }
        if (status != null) {
            wrapper.eq(Sku::getStatus, status);
        }
        return wrapper;
    }
}