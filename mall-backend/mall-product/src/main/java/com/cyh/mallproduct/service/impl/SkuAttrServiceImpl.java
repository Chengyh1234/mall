package com.cyh.mallproduct.service.impl;

import com.alibaba.fastjson2.JSON;
import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallcommon.utils.RedisUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallproduct.dto.SkuBatchCreateDto;
import com.cyh.mallproduct.dto.SkuUpdateDto;
import com.cyh.mallproduct.entity.*;
import com.cyh.mallproduct.mapper.*;
import com.cyh.mallproduct.mq.event.CacheDomain;
import com.cyh.mallproduct.mq.event.CacheInvalidateEvent;
import com.cyh.mallproduct.mq.publisher.CacheEventPublisher;
import com.cyh.mallproduct.service.SkuAttrService;
import com.cyh.mallproduct.service.SpuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SKU属性管理服务实现类
 * 提供商家操作SKU销售属性的功能实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkuAttrServiceImpl implements SkuAttrService {

    private final SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;
    private final SpuSaleAttrChoiceMapper spuSaleAttrChoiceMapper;
    private final AttributeValueMapper attributeValueMapper;
    private final SpuService spuService;
    private final CacheEventPublisher cacheEventPublisher;
    private final RedisUtils redisUtils;

    /**
     * 校验SKU属性组合是否合法
     * 1. 检查SPU是否选择了销售属性
     * 2. 收集SPU每个属性可选的属性值ID列表
     * 3. 检查SKU的属性值是否都在SPU的可选值列表中，且选择了每个SPU选择的属性
     * @param spuId        SPU ID
     * @param attrValueIds 属性值ID列表
     * @return 是否合法绑定
     */
    @Override
    public boolean validateAttrCombination(Long spuId, List<Long> attrValueIds) {
        if (CollectionUtils.isEmpty(attrValueIds)) {
            return true;
        }

        LambdaQueryWrapper<SpuSaleAttrChoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpuSaleAttrChoice::getSpuId, spuId);
        List<SpuSaleAttrChoice> choices = spuSaleAttrChoiceMapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(choices)) {
            return false;
        }

        List<Long> allowedValueIds = new ArrayList<>();
        Map<Long, List<Long>> attrIdToValueIdsMap = new HashMap<>();
        for (SpuSaleAttrChoice choice : choices) {
            List<Long> valueIds = JSON.parseArray(choice.getSelectedValues(), Long.class);
            if (!CollectionUtils.isEmpty(valueIds)) {
                allowedValueIds.addAll(valueIds);
                attrIdToValueIdsMap.put(choice.getAttrId(), valueIds);
            }
        }
        for (Long attrValueId : attrValueIds) {
            if (!allowedValueIds.contains(attrValueId)) {
                log.warn("属性值 {} 不在SPU {} 的可选值列表中", attrValueId, spuId);
                return false;
            }
        }

        List<AttributeValue> attrValues = attributeValueMapper.selectBatchIds(attrValueIds);

        Map<Long, Long> attrIdToValueIdMap  = attrValues.stream().collect(Collectors.toMap(AttributeValue::getAttrId, AttributeValue::getId));

        for (Long attrId : attrIdToValueIdsMap.keySet()) {
            Long valueId = attrIdToValueIdMap.get(attrId);
            if (valueId == null) {
                log.warn("属性 {} 没有对应的属性值", attrId);
                return false;
            }
        }

        return true;
    }

    /**
     * 校验商家是否有权限操作该SKU
     *
     * @param skuId    SKU ID
     * @param sellerId 商家ID
     * @return SKU实体
     */
    private Sku checkSkuPermission(Long skuId, Long sellerId) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }

        Spu spu = spuMapper.selectById(sku.getSpuId());
        if (spu == null) {
            throw new BusinessException("SPU不存在");
        }

        if (!spu.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作该SKU");
        }

        return sku;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Long, Integer> batchCreateSkuWithAttrs(List<SkuBatchCreateDto> dtoList, Long sellerId) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return Collections.emptyMap();
        }

        Set<Long> spuIds = dtoList.stream()
                .map(SkuBatchCreateDto::getSpuId)
                .collect(Collectors.toSet());
        if (spuIds.size() != 1) {
            throw new BusinessException("批量创建的所有SKU必须属于同一个SPU");
        }

        Long spuId = spuIds.iterator().next();

        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("SPU不存在: " + spuId);
        }
        if (!spu.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作该SPU");
        }

        List<List<Long>> combinations = dtoList.stream()
                .map(SkuBatchCreateDto::getAttrValueIds)
                .collect(Collectors.toList());
        checkDuplicateSkuCombination(spuId, combinations);

        Map<Long, Integer> resultMap = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (SkuBatchCreateDto dto : dtoList) {
            if (!validateAttrCombination(spuId, dto.getAttrValueIds())) {
                throw new BusinessException("SKU属性组合不合法，请检查属性值是否属于该SPU的销售属性");
            }

            if (dto.getPrice() == null || dto.getMarketPrice() == null || dto.getCostPrice() == null) {
                throw new BusinessException("销售价格、市场价和成本价不能为空");
            }

            Sku sku = new Sku();
            sku.setSpuId(spuId);
            sku.setPrice(dto.getPrice());
            sku.setMarketPrice(dto.getMarketPrice());
            sku.setCostPrice(dto.getCostPrice());
            sku.setStock(dto.getStock() != null ? dto.getStock() : 0);
            sku.setWarnStock(dto.getWarnStock() != null ? dto.getWarnStock() : 10);
            sku.setImage(dto.getImage());
            sku.setWeight(dto.getWeight());
            sku.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
            sku.setCreatedAt(now);
            sku.setUpdatedAt(now);
            skuMapper.insert(sku);

            int count = 0;
            for (Long attrValueId : dto.getAttrValueIds()) {
                SkuSaleAttrValue entity = new SkuSaleAttrValue();
                entity.setSkuId(sku.getId());
                entity.setAttrValueId(attrValueId);
                entity.setCreatedAt(now);
                entity.setUpdatedAt(now);
                skuSaleAttrValueMapper.insert(entity);
                count++;
            }

            resultMap.put(sku.getId(), count);
            // 初始化库存缓存，确保订单流程能正确读取库存
            if (dto.getStock() != null && dto.getStock() > 0) {
                redisUtils.set(RedisConstants.STOCK_PREFIX + sku.getId(), String.valueOf(dto.getStock()));
            }
            log.info("商家 {} 创建SKU {} 并绑定 {} 个销售属性", sellerId, sku.getId(), count);
        }

        log.info("商家 {} 批量创建SKU并绑定销售属性完成，共创建 {} 个SKU", sellerId, dtoList.size());

        spuService.updateMinPriceForSpu(spuId);

        // 事务提交后，异步清除SKU缓存
        publishSkuCacheInvalidate(spuId);

        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSkuWithAttrs(SkuBatchCreateDto dto, Long sellerId) {
        Long spuId = dto.getSpuId();

        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("SPU不存在");
        }
        if (!spu.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作该SPU");
        }

        if (!validateAttrCombination(spuId, dto.getAttrValueIds())) {
            throw new BusinessException("SKU属性组合不合法，请检查属性值是否属于该SPU的销售属性");
        }

        checkDuplicateSkuCombination(spuId, Collections.singletonList(dto.getAttrValueIds()));

        if (dto.getPrice() == null || dto.getMarketPrice() == null || dto.getCostPrice() == null) {
            throw new BusinessException("销售价格、市场价和成本价不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        sku.setPrice(dto.getPrice());
        sku.setMarketPrice(dto.getMarketPrice());
        sku.setCostPrice(dto.getCostPrice());
        sku.setStock(dto.getStock() != null ? dto.getStock() : 0);
        sku.setWarnStock(dto.getWarnStock() != null ? dto.getWarnStock() : 10);
        sku.setImage(dto.getImage());
        sku.setWeight(dto.getWeight());
        sku.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        sku.setCreatedAt(now);
        sku.setUpdatedAt(now);
        skuMapper.insert(sku);

        for (Long attrValueId : dto.getAttrValueIds()) {
            SkuSaleAttrValue entity = new SkuSaleAttrValue();
            entity.setSkuId(sku.getId());
            entity.setAttrValueId(attrValueId);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            skuSaleAttrValueMapper.insert(entity);
        }

        log.info("商家 {} 创建SKU {} 并绑定销售属性", sellerId, sku.getId());

        // 初始化库存缓存，确保订单流程能正确读取库存
        if (dto.getStock() != null && dto.getStock() > 0) {
            redisUtils.set(RedisConstants.STOCK_PREFIX + sku.getId(), String.valueOf(dto.getStock()));
        }

        spuService.updateMinPriceForSpu(spuId);

        // 事务提交后，异步清除SKU缓存
        publishSkuCacheInvalidate(spuId);

        return sku.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSkuWithAttrs(SkuUpdateDto dto, Long sellerId) {
        Long skuId = dto.getId();

        Sku sku = checkSkuPermission(skuId, sellerId);

        boolean needUpdateSku = dto.getPrice() != null
                || dto.getMarketPrice() != null
                || dto.getCostPrice() != null
                || dto.getStock() != null
                || dto.getWarnStock() != null
                || dto.getImage() != null
                || dto.getWeight() != null
                || dto.getStatus() != null;

        if (needUpdateSku) {
            if (dto.getPrice() == null || dto.getMarketPrice() == null || dto.getCostPrice() == null) {
                throw new BusinessException("销售价格、市场价和成本价不能为空");
            }
            Sku updateSku = new Sku();
            updateSku.setId(skuId);
            updateSku.setPrice(dto.getPrice());
            updateSku.setMarketPrice(dto.getMarketPrice());
            updateSku.setCostPrice(dto.getCostPrice());
            updateSku.setStock(dto.getStock());
            updateSku.setWarnStock(dto.getWarnStock());
            updateSku.setImage(dto.getImage());
            updateSku.setWeight(dto.getWeight());
            updateSku.setStatus(dto.getStatus());
            updateSku.setUpdatedAt(LocalDateTime.now());
            skuMapper.updateById(updateSku);

            // 同步库存缓存，保证 Redis 库存 Key 与 DB 一致
            if (dto.getStock() != null) {
                redisUtils.set(RedisConstants.STOCK_PREFIX + skuId, String.valueOf(dto.getStock()));
            }
        }

        log.info("商家 {} 更新SKU {} 信息成功（不修改销售属性）", sellerId, skuId);

        spuService.updateMinPriceForSpu(sku.getSpuId());

        // 事务提交后，异步清除SKU缓存
        publishSkuCacheInvalidate(sku.getSpuId());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateSkuWithAttrs(List<SkuUpdateDto> dtoList, Long sellerId) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return 0;
        }

        int successCount = 0;
        for (SkuUpdateDto dto : dtoList) {
            try {
                updateSkuWithAttrs(dto, sellerId);
                successCount++;
            } catch (Exception e) {
                log.error("批量更新SKU失败，SKU ID: {}，错误: {}", dto.getId(), e.getMessage());
            }
        }

        log.info("商家 {} 批量更新SKU完成，成功: {}，共: {}", sellerId, successCount, dtoList.size());
        return successCount;
    }

    /**
     * 检查SKU属性值组合是否已存在（同SPU下去重）
     */
    private void checkDuplicateSkuCombination(Long spuId, List<List<Long>> newCombinations) {
        List<Sku> existingSkus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getSpuId, spuId)
                        .select(Sku::getId)
        );

        if (CollectionUtils.isEmpty(existingSkus)) {
            return;
        }

        List<Long> existingSkuIds = existingSkus.stream()
                .map(Sku::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<SkuSaleAttrValue> valueWrapper = new LambdaQueryWrapper<>();
        valueWrapper.in(SkuSaleAttrValue::getSkuId, existingSkuIds);
        List<SkuSaleAttrValue> allValues = skuSaleAttrValueMapper.selectList(valueWrapper);

        Map<Long, List<Long>> existSkuAttrMap = allValues.stream()
                .collect(Collectors.groupingBy(
                        SkuSaleAttrValue::getSkuId,
                        Collectors.mapping(SkuSaleAttrValue::getAttrValueId, Collectors.toList())
                ));

        existSkuAttrMap.values().forEach(Collections::sort);

        Set<String> existCombinationSet = existSkuAttrMap.values().stream()
                .map(list -> list.stream().map(String::valueOf).collect(Collectors.joining("-")))
                .collect(Collectors.toSet());

        for (List<Long> newCombination : newCombinations) {
            if (CollectionUtils.isEmpty(newCombination)) {
                continue;
            }
            List<Long> sorted = new ArrayList<>(newCombination);
            Collections.sort(sorted);
            String key = sorted.stream().map(String::valueOf).collect(Collectors.joining("-"));
            if (existCombinationSet.contains(key)) {
                throw new BusinessException("属性值组合[" + key + "] 对应的SKU已存在，请勿重复添加");
            }
        }
    }

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
}