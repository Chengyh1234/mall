package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallportal.entity.*;
import com.cyh.mallportal.mapper.*;
import com.cyh.mallportal.service.SkuService;
import com.cyh.mallportal.service.SpuService;
import com.cyh.mallportal.vo.AdminVo;
import com.cyh.mallportal.vo.SkuStoreVo;
import com.cyh.mallportal.vo.SkuVo;
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
    //@Override
    //public List<Sku> getPage(Long spuId, Integer status, Integer page, Integer pageSize) {
    //    LambdaQueryWrapper<Sku> wrapper = buildWrapper(spuId, status);
    //    wrapper.orderByAsc(Sku::getId);
    //
    //    // 分页计算
    //    int offset = (page - 1) * pageSize;
    //    long totalLong = skuMapper.selectCount(wrapper);
    //    int total = (int) totalLong;
    //
    //    List<Sku> allList = skuMapper.selectList(wrapper);
    //    int fromIndex = Math.min(offset, total);
    //    int toIndex = Math.min(offset + pageSize, total);
    //
    //    return allList.subList(fromIndex, toIndex);
    //}

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
    //    @Override
//    public BigDecimal getMinPrice(Long spuId) {
//        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(Sku::getSpuId, spuId);
//        wrapper.eq(Sku::getStatus, 1);
//        wrapper.orderByAsc(Sku::getPrice);
//        wrapper.last("LIMIT 1");
//
//        Sku sku = skuMapper.selectOne(wrapper);
//        return sku != null ? sku.getPrice() : BigDecimal.ZERO;
//    }

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
     * 公开接口，仅返回启用状态（status=1）的SKU
     *
     * @param spuId SPU ID
     * @return SKU列表（包含销售属性，公开字段）
     */
    @Override
    public List<SkuVo> getBySpuIdWithAttributes(Long spuId) {
        log.info("获取SPU的SKU列表（包含销售属性）, spuId: {}", spuId);

        List<Sku> skus = getBySpuId(spuId);
        if (skus == null || skus.isEmpty()) {
            return new ArrayList<>();
        }

        return skus.stream()
                .filter(sku -> sku.getStatus() != null && sku.getStatus() == 1) // 仅返回启用状态的SKU
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }

    /**
     * 商家端：根据SPU ID获取SKU列表（包含销售属性）
     * 返回商家经营管理所需的完整字段，不限SKU上下架状态
     *
     * @param spuId SPU ID
     * @return SKU列表（包含销售属性，商家端字段）
     */
    @Override
    public List<SkuStoreVo> getStoreBySpuIdWithAttributes(Long spuId) {
        log.info("商家端获取SPU的SKU列表（包含销售属性）, spuId: {}", spuId);

        List<Sku> skus = getBySpuId(spuId);
        if (skus == null || skus.isEmpty()) {
            return new ArrayList<>();
        }

        return skus.stream()
                .map(this::convertToStoreVo)
                .collect(Collectors.toList());
    }

    /**
     * 管理员端：根据SPU ID获取SKU列表（包含销售属性）
     * 返回管理员监管所需的全部字段，不限SKU上下架和删除状态
     *
     * @param spuId SPU ID
     * @return SKU列表（包含销售属性，管理员端字段）
     */
    @Override
    public List<AdminVo> getAdminBySpuIdWithAttributes(Long spuId) {
        log.info("管理员端获取SPU的SKU列表（包含销售属性）, spuId: {}", spuId);

        List<Sku> skus = getBySpuId(spuId);
        if (skus == null || skus.isEmpty()) {
            return new ArrayList<>();
        }

        return skus.stream()
                .map(this::convertToAdminVo)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取SKU详情（包含销售属性）
     *
     * @param id SKU ID
     * @return SKU详情（包含销售属性）
     */
    //@Override
    //public SkuVo getByIdWithAttributes(Long id) {
    //    log.info("获取SKU详情（包含销售属性）, id: {}", id);
    //
    //    Sku sku = getById(id);
    //    if (sku == null) {
    //        return null;
    //    }
    //
    //    return convertToVo(sku);
    //}

    /**
     * 将SKU实体转换为公开VO（普通用户可见）
     */
    private SkuVo convertToVo(Sku sku) {
        SkuVo vo = SkuVo.builder()
                .id(sku.getId())
                .spuId(sku.getSpuId())
                .price(sku.getPrice())
                .marketPrice(sku.getMarketPrice())
                .image(sku.getImage())
                .weight(sku.getWeight())
                .inStock(sku.getStock() != null && sku.getStock() > 0)
                .build();

        // 获取销售属性
        List<Map<String, Object>> attributes = getSkuSaleAttributes(sku.getId());
        vo.setSaleAttributes(attributes);

        return vo;
    }

    /**
     * 将SKU实体转换为商家端VO
     */
    private SkuStoreVo convertToStoreVo(Sku sku) {
        SkuStoreVo vo = new SkuStoreVo();
        // 父类字段
        vo.setId(sku.getId());
        vo.setSpuId(sku.getSpuId());
        vo.setPrice(sku.getPrice());
        vo.setMarketPrice(sku.getMarketPrice());
        vo.setImage(sku.getImage());
        vo.setWeight(sku.getWeight());
        vo.setInStock(sku.getStock() != null && sku.getStock() > 0);
        // 商家字段
        vo.setCostPrice(sku.getCostPrice());
        vo.setStock(sku.getStock());
        vo.setWarnStock(sku.getWarnStock());
        vo.setStatus(sku.getStatus());
        vo.setCreatedAt(sku.getCreatedAt());
        vo.setUpdatedAt(sku.getUpdatedAt());

        // 获取销售属性
        List<Map<String, Object>> attributes = getSkuSaleAttributes(sku.getId());
        vo.setSaleAttributes(attributes);

        return vo;
    }

    /**
     * 将SKU实体转换为管理员端VO
     */
    private AdminVo convertToAdminVo(Sku sku) {
        AdminVo vo = new AdminVo();
        // 父类字段（含商家字段）
        vo.setId(sku.getId());
        vo.setSpuId(sku.getSpuId());
        vo.setPrice(sku.getPrice());
        vo.setMarketPrice(sku.getMarketPrice());
        vo.setImage(sku.getImage());
        vo.setWeight(sku.getWeight());
        vo.setInStock(sku.getStock() != null && sku.getStock() > 0);
        vo.setCostPrice(sku.getCostPrice());
        vo.setStock(sku.getStock());
        vo.setWarnStock(sku.getWarnStock());
        vo.setStatus(sku.getStatus());
        vo.setCreatedAt(sku.getCreatedAt());
        vo.setUpdatedAt(sku.getUpdatedAt());
        // 管理员字段
        vo.setFrozenStock(sku.getFrozenStock());
        vo.setIsDeleted(sku.getIsDeleted());

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
