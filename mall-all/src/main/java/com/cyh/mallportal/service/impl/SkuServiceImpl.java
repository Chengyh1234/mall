package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallportal.entity.*;
import com.cyh.mallportal.mapper.*;
import com.cyh.mallportal.service.SkuService;
import com.cyh.mallportal.service.SpuService;
import com.cyh.mallportal.vo.SkuVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SKU服务实现类
 * 提供商品库存单元业务逻辑的具体实现
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


    /**
     * 新增SKU
     *
     * @param sku SKU实体
     * @return SKU ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(Sku sku) {

        if (sku.getStatus() == null) {
            sku.setStatus(1);
        }
        if (sku.getStock() == null) {
            sku.setStock(0);
        }
        if (sku.getWarnStock() == null) {
            sku.setWarnStock(10);
        }

        sku.setCreatedAt(LocalDateTime.now());
        sku.setUpdatedAt(LocalDateTime.now());

        skuMapper.insert(sku);
        log.info("新增SKU成功, ID: {}", sku.getId());

        spuService.updateMinPriceForSpu(sku.getSpuId());

        // 新增SKU后刷新SPU状态：新增的SKU默认为启用状态，应触发SPU上架
        spuService.refreshSpuStatus(sku.getSpuId());

        return sku.getId();
    }

    /**
     * 批量新增SKU
     *
     * @param skus SKU列表
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAdd(List<Sku> skus) {
        log.info("批量新增SKU, 数量: {}", skus.size());

        if (skus == null || skus.isEmpty()) {
            log.warn("批量新增SKU为空");
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Sku sku : skus) {
            if (sku.getStatus() == null) {
                sku.setStatus(1);
            }
            if (sku.getStock() == null) {
                sku.setStock(0);
            }
            if (sku.getWarnStock() == null) {
                sku.setWarnStock(10);
            }
            sku.setCreatedAt(now);
            sku.setUpdatedAt(now);
        }

        // 使用循环插入实现批量新增
        for (Sku sku : skus) {
            skuMapper.insert(sku);
        }

        log.info("批量新增SKU成功");

        Set<Long> spuIds = skus.stream().map(Sku::getSpuId).collect(Collectors.toSet());
        for (Long spuId : spuIds) {
            spuService.updateMinPriceForSpu(spuId);
            // 批量新增SKU后刷新SPU状态
            spuService.refreshSpuStatus(spuId);
        }

        return true;
    }

    /**
     * 删除SKU（逻辑删除）
     *
     * @param id SKU ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        log.info("删除SKU: {}", id);

        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            log.warn("SKU不存在: {}", id);
            return false;
        }

        // 逻辑删除：调用deleteById让MyBatis-Plus自动转换为UPDATE语句
        skuMapper.deleteById(id);

        spuService.updateMinPriceForSpu(sku.getSpuId());

        // 删除SKU后刷新SPU状态：删除唯一启用SKU时应下架SPU
        spuService.refreshSpuStatus(sku.getSpuId());

        log.info("删除SKU成功: {}", id);
        return true;
    }

    /**
     * 根据SPU ID删除所有SKU（逻辑删除）
     *
     * @param spuId SPU ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBySpuId(Long spuId) {
        log.info("删除SPU下所有SKU, SPU ID: {}", spuId);

        // 先删除SKU的销售属性绑定
        List<Sku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spuId));
        for (Sku sku : skus) {
            skuSaleAttrValueMapper.delete(
                    new LambdaQueryWrapper<SkuSaleAttrValue>().eq(SkuSaleAttrValue::getSkuId, sku.getId()));
        }

        // 逻辑删除SKU
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getSpuId, spuId);
        skuMapper.delete(wrapper);

        spuService.updateMinPriceForSpu(spuId);

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

        // 删除前获取每个SKU的spuId，用于后续更新minPrice
        Map<Long, Long> idToSpuIdMap = new HashMap<>();
        for (Long id : ids) {
            Sku sku = skuMapper.selectById(id);
            if (sku != null) {
                idToSpuIdMap.put(id, sku.getSpuId());
            }
        }

        // 先删除SKU的销售属性绑定
        for (Long id : ids) {
            skuSaleAttrValueMapper.delete(
                    new LambdaQueryWrapper<SkuSaleAttrValue>().eq(SkuSaleAttrValue::getSkuId, id));
        }

        // 逻辑删除SKU
        for (Long id : ids) {
            skuMapper.deleteById(id);
        }

        Set<Long> affectedSpuIds = new HashSet<>(idToSpuIdMap.values());
        for (Long spuId : affectedSpuIds) {
            spuService.updateMinPriceForSpu(spuId);
            // 批量删除SKU后刷新SPU状态
            spuService.refreshSpuStatus(spuId);
        }

        log.info("批量删除SKU完成, 数量: {}", ids.size());
        return ids.size();
    }

    /**
     * 更新SKU信息
     *
     * @param sku SKU实体
     * @return 是否更新成功
     */
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
                // 修改SKU后刷新SPU状态：SKU状态变更可能影响SPU的上架/下架
                spuService.refreshSpuStatus(updatedSku.getSpuId());
            }
        } else {
            log.warn("更新SKU失败: {}", sku.getId());
        }

        return success;
    }

    /**
     * 更新库存
     *
     * @param id    SKU ID
     * @param stock 库存数量
     * @return 是否更新成功
     */
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

        return rows > 0;
    }

    /**
     * 扣减库存
     *
     * @param id       SKU ID
     * @param quantity 扣减数量
     * @return 是否扣减成功
     */
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

        log.info("扣减库存成功, SKU: {}, 剩余库存: {}", id, sku.getStock());
        return true;
    }

    /**
     * 根据ID获取SKU详情
     *
     * @param id SKU ID
     * @return SKU实体
     */
    @Override
    public Sku getById(Long id) {
        return skuMapper.selectById(id);
    }

    /**
     * 根据SPU ID获取SKU列表
     *
     * @param spuId SPU ID
     * @return SKU列表
     */
    @Override
    public List<Sku> getBySpuId(Long spuId) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getSpuId, spuId);
        wrapper.orderByAsc(Sku::getId);
        return skuMapper.selectList(wrapper);
    }

    /**
     * 获取所有SKU列表
     *
     * @return SKU列表
     */
    @Override
    public List<Sku> getAll() {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Sku::getId);
        return skuMapper.selectList(wrapper);
    }

    /**
     * 获取SKU列表（分页）
     *
     * @param spuId    SPU ID（可选）
     * @param status   状态（可选）
     * @param page     页码
     * @param pageSize 每页条数
     * @return SKU列表
     */
    @Override
    public List<Sku> getPage(Long spuId, Integer status, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Sku> wrapper = buildWrapper(spuId, status);
        wrapper.orderByAsc(Sku::getId);

        // 分页计算
        int offset = (page - 1) * pageSize;
        long totalLong = skuMapper.selectCount(wrapper);
        int total = (int) totalLong;

        List<Sku> allList = skuMapper.selectList(wrapper);
        int fromIndex = Math.min(offset, total);
        int toIndex = Math.min(offset + pageSize, total);

        return allList.subList(fromIndex, toIndex);
    }

    /**
     * 获取SKU总数
     *
     * @param spuId  SPU ID（可选）
     * @param status 状态（可选）
     * @return 总数
     */
    @Override
    public int count(Long spuId, Integer status) {
        LambdaQueryWrapper<Sku> wrapper = buildWrapper(spuId, status);
        long totalLong = skuMapper.selectCount(wrapper);
        return (int) totalLong;
    }

    /**
     * 启用SKU（设置 status=1）
     *
     * @param id SKU ID
     * @return 是否启用成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enable(Long id) {
        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            return false;
        }
        sku.setStatus(1);
        sku.setUpdatedAt(LocalDateTime.now());
        return skuMapper.updateById(sku) > 0;
    }

    /**
     * 禁用SKU（设置 status=0）
     *
     * @param id SKU ID
     * @return 是否禁用成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disable(Long id) {
        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            return false;
        }
        sku.setStatus(0);
        sku.setUpdatedAt(LocalDateTime.now());
        return skuMapper.updateById(sku) > 0;
    }

    /**
     * 获取SPU的最低价格
     *
     * @param spuId SPU ID
     * @return 最低价格
     */
    @Override
    public BigDecimal getMinPrice(Long spuId) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getSpuId, spuId);
        wrapper.eq(Sku::getStatus, 1);
        wrapper.orderByAsc(Sku::getPrice);
        wrapper.last("LIMIT 1");

        Sku sku = skuMapper.selectOne(wrapper);
        return sku != null ? sku.getPrice() : BigDecimal.ZERO;
    }

    /**
     * 获取SPU的库存总量
     *
     * @param spuId SPU ID
     * @return 库存总量
     */
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

    /**
     * 根据SPU ID获取SKU列表（包含销售属性）
     *
     * @param spuId SPU ID
     * @return SKU列表（包含销售属性）
     */
    @Override
    public List<SkuVo> getBySpuIdWithAttributes(Long spuId) {
        log.info("获取SPU的SKU列表（包含销售属性）, spuId: {}", spuId);

        List<Sku> skus = getBySpuId(spuId);
        if (skus == null || skus.isEmpty()) {
            return new ArrayList<>();
        }

        return skus.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取SKU详情（包含销售属性）
     *
     * @param id SKU ID
     * @return SKU详情（包含销售属性）
     */
    @Override
    public SkuVo getByIdWithAttributes(Long id) {
        log.info("获取SKU详情（包含销售属性）, id: {}", id);

        Sku sku = getById(id);
        if (sku == null) {
            return null;
        }

        return convertToVo(sku);
    }

    /**
     * 将SKU实体转换为VO（包含销售属性）
     */
    private SkuVo convertToVo(Sku sku) {
        SkuVo vo = SkuVo.builder()
                .id(sku.getId())
                .spuId(sku.getSpuId())
                .price(sku.getPrice())
                .marketPrice(sku.getMarketPrice())
                .costPrice(sku.getCostPrice())
                .stock(sku.getStock())
                .warnStock(sku.getWarnStock())
                .image(sku.getImage())
                .weight(sku.getWeight())
                .status(sku.getStatus())
                .createdAt(sku.getCreatedAt())
                .updatedAt(sku.getUpdatedAt())
                .build();

        // 获取销售属性
        List<Map<String, Object>> attributes = getSkuSaleAttributes(sku.getId());
        vo.setSaleAttributes(attributes);

        return vo;
    }

    /**
     * 获取SKU的销售属性列表
     */
    private List<Map<String, Object>> getSkuSaleAttributes(Long skuId) {
        // 1. 获取SKU关联的属性值关系
        List<SkuSaleAttrValue> skuAttrValues = skuSaleAttrValueMapper.getBySkuId(skuId);
        if (skuAttrValues == null || skuAttrValues.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 批量获取所有属性值ID并查询属性值信息
        List<Long> attrValueIds = skuAttrValues.stream()
                .map(SkuSaleAttrValue::getAttrValueId)
                .distinct()
                .collect(Collectors.toList());

        List<AttributeValue> attrValues = attributeValueMapper.selectBatchIds(attrValueIds);
        Map<Long, AttributeValue> attrValueMap = attrValues.stream()
                .collect(Collectors.toMap(AttributeValue::getId, v -> v));

        // 3. 批量获取所有属性ID并查询属性信息
        List<Long> attrIds = attrValues.stream()
                .map(AttributeValue::getAttrId)
                .distinct()
                .collect(Collectors.toList());

        List<Attribute> attributes = attributeMapper.selectBatchIds(attrIds);
        Map<Long, Attribute> attrMap = attributes.stream()
                .collect(Collectors.toMap(Attribute::getId, a -> a));

        // 4. 构建结果
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

    /**
     * 构建查询条件
     */
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
