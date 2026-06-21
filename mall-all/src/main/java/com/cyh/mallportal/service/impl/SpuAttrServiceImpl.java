package com.cyh.mallportal.service.impl;

import com.alibaba.fastjson2.JSON;
import com.cyh.mallcommon.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cyh.mallportal.dto.SpuAttrFullBindDto;
import com.cyh.mallportal.dto.SpuBasicAttrBindDto;
import com.cyh.mallportal.dto.SpuSaleAttrBindDto;
import com.cyh.mallportal.entity.*;
import com.cyh.mallportal.mapper.*;
import com.cyh.mallportal.service.SpuAttrService;
import com.cyh.mallportal.vo.SpuAttrVo;
import com.cyh.mallportal.vo.SpuAvailableAttrVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SPU属性管理服务实现类
 * 提供商家操作SPU基本属性和销售属性的功能实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpuAttrServiceImpl implements SpuAttrService {

    private final SpuBasicAttrValueMapper spuBasicAttrValueMapper;
    private final SpuSaleAttrChoiceMapper spuSaleAttrChoiceMapper;
    private final SpuMapper spuMapper;
    private final AttributeMapper attributeMapper;
    private final AttributeValueMapper attributeValueMapper;
    private final SkuMapper skuMapper;
    private final SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    /**
     * 绑定SPU基本属性
     *
     * @param dto      基本属性绑定参数
     * @param sellerId 商家ID
     * @return 绑定记录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bindBasicAttr(SpuBasicAttrBindDto dto, Long sellerId) {
        checkSpuPermission(dto.getSpuId(), sellerId);

        // 校验属性是否为基本属性（attr_type = 2）
        Attribute attr = attributeMapper.selectById(dto.getAttrId());
        if (attr == null) {
            throw new BusinessException("属性不存在");
        }
        if (attr.getAttrType() != 2) {
            throw new BusinessException("该属性不是基本属性，无法绑定");
        }

        // 校验attrValueId和manualValue至少有一项
        if (dto.getAttrValueId() == null && !StringUtils.hasText(dto.getManualValue())) {
            throw new BusinessException("属性值ID和手动输入值至少填写一项");
        }

        // 如果提供了attrValueId，校验是否存在
        if (dto.getAttrValueId() != null) {
            AttributeValue attrValue = attributeValueMapper.selectById(dto.getAttrValueId());
            if (attrValue == null) {
                throw new BusinessException("属性值不存在");
            }
            if (!attrValue.getAttrId().equals(dto.getAttrId())) {
                throw new BusinessException("属性值不属于该属性");
            }
        }

        // 检查是否已存在绑定关系
        LambdaQueryWrapper<SpuBasicAttrValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpuBasicAttrValue::getSpuId, dto.getSpuId())
               .eq(SpuBasicAttrValue::getAttrId, dto.getAttrId());
        SpuBasicAttrValue exist = spuBasicAttrValueMapper.selectOne(wrapper);
        if (exist != null) {
            throw new BusinessException("该基本属性已绑定，请先解绑或更新");
        }

        // attrValueId 和 manualValue 互斥处理
        //    仅传入manualValue 时 attrValueId=null, 保留manualValue
        //    仅传入attrValueId 时, 保留attrValueId, manualValue=null
        //    两者都有时, attrValueId=null, 保留manualValue（手工值优先）
        SpuBasicAttrValue entity = new SpuBasicAttrValue();
        entity.setSpuId(dto.getSpuId());
        entity.setAttrId(dto.getAttrId());
        if (StringUtils.hasText(dto.getManualValue())) {
            entity.setAttrValueId(null);
            entity.setManualValue(dto.getManualValue());
        } else {
            entity.setAttrValueId(dto.getAttrValueId());
            entity.setManualValue(null);
        }
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        spuBasicAttrValueMapper.insert(entity);
        log.info("商家 {} 为SPU {} 绑定基本属性 {} 成功", sellerId, dto.getSpuId(), dto.getAttrId());

        return entity.getId();
    }

    /**
     * 批量绑定SPU基本属性（逐个绑定，单条失败不影响其他）
     * <p>
     * 遍历传入的绑定列表，逐个执行绑定操作。
     * 某个绑定失败不会影响其他绑定的执行，最终返回成功绑定的数量。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchBindBasicAttr(List<SpuBasicAttrBindDto> dtoList, Long sellerId) {
        if (CollectionUtils.isEmpty(dtoList)) {
            log.warn("批量绑定基本属性列表为空，跳过处理");
            return 0;
        }

        int count = 0;
        for (SpuBasicAttrBindDto dto : dtoList) {
            try {
                bindBasicAttr(dto, sellerId);
                count++;
            } catch (Exception e) {
                log.error("批量绑定基本属性失败，SPU ID: {}，属性ID: {}，错误: {}",
                        dto.getSpuId(), dto.getAttrId(), e.getMessage());
            }
        }
        return count;
    }

    /**
     * 更新SPU基本属性绑定
     *
     * @param id       绑定记录ID
     * @param dto      基本属性绑定参数
     * @param sellerId 商家ID
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBasicAttr(Long id, SpuBasicAttrBindDto dto, Long sellerId) {
        // 查询原记录
        SpuBasicAttrValue entity = spuBasicAttrValueMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("绑定记录不存在");
        }

        // 校验商家权限
        checkSpuPermission(entity.getSpuId(), sellerId);

        // 校验attrValueId和manualValue至少有一项
        if (dto.getAttrValueId() == null && !StringUtils.hasText(dto.getManualValue())) {
            throw new BusinessException("属性值ID和手动输入值至少填写一项");
        }

        // 如果提供了attrValueId，校验是否存在
        if (dto.getAttrValueId() != null) {
            AttributeValue attrValue = attributeValueMapper.selectById(dto.getAttrValueId());
            if (attrValue == null) {
                throw new BusinessException("属性值不存在");
            }
            if (!attrValue.getAttrId().equals(entity.getAttrId())) {
                throw new BusinessException("属性值不属于该属性");
            }
        }

        // attrValueId 和 manualValue 互斥处理：手工值优先
        LambdaUpdateWrapper<SpuBasicAttrValue> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SpuBasicAttrValue::getId, entity.getId());
        if (StringUtils.hasText(dto.getManualValue())) {
            updateWrapper.set(SpuBasicAttrValue::getAttrValueId, null);
            updateWrapper.set(SpuBasicAttrValue::getManualValue, dto.getManualValue());
        } else {
            updateWrapper.set(SpuBasicAttrValue::getAttrValueId, dto.getAttrValueId());
            updateWrapper.set(SpuBasicAttrValue::getManualValue, null);
        }
        updateWrapper.set(SpuBasicAttrValue::getUpdatedAt, LocalDateTime.now());
        int result = spuBasicAttrValueMapper.update(null, updateWrapper);
        log.info("商家 {} 更新SPU基本属性绑定 {} 成功", sellerId, id);

        return result > 0;
    }

    /**
     * 批量更新SPU基本属性绑定（全校验后统一更新，任一失败则全部回滚）
     * <p>
     * 先遍历校验所有更新项，全部校验通过后再逐一执行更新。
     * 如果任何一项存在数据不存在、权限不足或参数无效的情况，全部回滚。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateBasicAttr(List<SpuBasicAttrBindDto> dtoList, Long sellerId) {
        // 前置校验：遍历所有更新项，检查是否存在冲突
        for (SpuBasicAttrBindDto dto : dtoList) {
            // 查询原记录
            SpuBasicAttrValue entity = spuBasicAttrValueMapper.selectById(dto.getId());
            if (entity == null) {
                throw new BusinessException("绑定记录不存在，ID: " + dto.getId());
            }

            // 校验商家权限
            checkSpuPermission(entity.getSpuId(), sellerId);

            // 校验attrValueId和manualValue至少有一项
            if (dto.getAttrValueId() == null && !StringUtils.hasText(dto.getManualValue())) {
                throw new BusinessException("属性值ID和手动输入值至少填写一项");
            }

            // 如果提供了attrValueId，校验是否存在
            if (dto.getAttrValueId() != null) {
                AttributeValue attrValue = attributeValueMapper.selectById(dto.getAttrValueId());
                if (attrValue == null) {
                    throw new BusinessException("属性值不存在: " + dto.getAttrValueId());
                }
                if (!attrValue.getAttrId().equals(entity.getAttrId())) {
                    throw new BusinessException("属性值" + dto.getAttrValueId() + " 不属于该属性");
                }
            }
        }

        // 所有校验通过后，逐一更新
        int count = 0;
        for (SpuBasicAttrBindDto dto : dtoList) {
            SpuBasicAttrValue entity = spuBasicAttrValueMapper.selectById(dto.getId());

            // attrValueId 和 manualValue 互斥处理：手工值优先
            LambdaUpdateWrapper<SpuBasicAttrValue> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SpuBasicAttrValue::getId, entity.getId());
            if (StringUtils.hasText(dto.getManualValue())) {
                updateWrapper.set(SpuBasicAttrValue::getAttrValueId, null);
                updateWrapper.set(SpuBasicAttrValue::getManualValue, dto.getManualValue());
            } else {
                updateWrapper.set(SpuBasicAttrValue::getAttrValueId, dto.getAttrValueId());
                updateWrapper.set(SpuBasicAttrValue::getManualValue, null);
            }
            updateWrapper.set(SpuBasicAttrValue::getUpdatedAt, LocalDateTime.now());
            spuBasicAttrValueMapper.update(null, updateWrapper);

            count++;
            log.info("商家 {} 更新SPU基本属性绑定 {} 成功", sellerId, dto.getId());
        }

        return count;
    }

    /**
     * 删除SPU基本属性绑定
     *
     * @param id       绑定记录ID
     * @param sellerId 商家ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBasicAttr(Long id, Long sellerId) {
        // 查询原记录
        SpuBasicAttrValue entity = spuBasicAttrValueMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("绑定记录不存在");
        }

        // 校验商家权限
        checkSpuPermission(entity.getSpuId(), sellerId);

        // 删除记录
        int result = spuBasicAttrValueMapper.deleteById(id);
        log.info("商家 {} 删除SPU基本属性绑定 {} 成功", sellerId, id);

        return result > 0;
    }

    /**
     * 获取SPU已绑定的基本属性列表
     *
     * @param spuId SPU ID
     * @return 基本属性绑定记录列表
     */
    @Override
    public List<SpuBasicAttrValue> getBasicAttrsBySpuId(Long spuId) {
        LambdaQueryWrapper<SpuBasicAttrValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpuBasicAttrValue::getSpuId, spuId);
        return spuBasicAttrValueMapper.selectList(wrapper);
    }

    /**
     * 绑定SPU销售属性
     *
     * @param dto      销售属性绑定参数
     * @param sellerId 商家ID
     * @return 绑定记录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bindSaleAttr(SpuSaleAttrBindDto dto, Long sellerId) {
        checkSpuPermission(dto.getSpuId(), sellerId);

        // 校验属性是否为销售属性（attr_type = 1）
        Attribute attr = attributeMapper.selectById(dto.getAttrId());
        if (attr == null) {
            throw new BusinessException("属性不存在");
        }
        if (attr.getAttrType() != 1) {
            throw new BusinessException("该属性不是销售属性，无法绑定");
        }

        // 校验属性值列表
        if (CollectionUtils.isEmpty(dto.getSelectedValueIds())) {
            throw new BusinessException("属性值ID列表不能为空");
        }

        // 校验所有属性值是否属于该属性
        for (Long valueId : dto.getSelectedValueIds()) {
            AttributeValue attrValue = attributeValueMapper.selectById(valueId);
            if (attrValue == null) {
                throw new BusinessException("属性值不存在: " + valueId);
            }
            if (!attrValue.getAttrId().equals(dto.getAttrId())) {
                throw new BusinessException("属性值" + valueId + " 不属于该属性");
            }
        }

        // 检查是否已存在绑定关系
        LambdaQueryWrapper<SpuSaleAttrChoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpuSaleAttrChoice::getSpuId, dto.getSpuId())
               .eq(SpuSaleAttrChoice::getAttrId, dto.getAttrId());
        SpuSaleAttrChoice exist = spuSaleAttrChoiceMapper.selectOne(wrapper);
        if (exist != null) {
            throw new BusinessException("该销售属性已绑定，请先解绑或更新");
        }

        // 保存绑定关系
        SpuSaleAttrChoice entity = new SpuSaleAttrChoice();
        entity.setSpuId(dto.getSpuId());
        entity.setAttrId(dto.getAttrId());
        entity.setSelectedValues(JSON.toJSONString(dto.getSelectedValueIds()));
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        spuSaleAttrChoiceMapper.insert(entity);
        log.info("商家 {} 为SPU {} 绑定销售属性 {} 成功，可选值: {}",
                sellerId, dto.getSpuId(), dto.getAttrId(), dto.getSelectedValueIds());

        return entity.getId();
    }

    /**
     * 批量绑定SPU销售属性（逐个绑定，单条失败不影响其他）
     *
     * @param dtoList  销售属性绑定参数列表
     * @param sellerId 商家ID
     * @return 成功绑定的数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchBindSaleAttr(List<SpuSaleAttrBindDto> dtoList, Long sellerId) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return 0;
        }

        int count = 0;
        for (SpuSaleAttrBindDto dto : dtoList) {
            try {
                bindSaleAttr(dto, sellerId);
                count++;
            } catch (Exception e) {
                log.error("批量绑定销售属性失败，SPU ID: {}，属性ID: {}，错误: {}",
                        dto.getSpuId(), dto.getAttrId(), e.getMessage());
            }
        }
        return count;
    }

    /**
     * 更新SPU销售属性绑定
     * <p>
     * 如果变更时移除了某个属性值，且该属性值已被SKU绑定，
     * 则抛出异常阻止更新，提示前端先处理相关SKU。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSaleAttr(Long id, SpuSaleAttrBindDto dto, Long sellerId) {
        // 查询原记录
        SpuSaleAttrChoice entity = spuSaleAttrChoiceMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("绑定记录不存在");
        }

        // 校验商家权限
        checkSpuPermission(entity.getSpuId(), sellerId);

        // 校验属性值列表
        if (CollectionUtils.isEmpty(dto.getSelectedValueIds())) {
            throw new BusinessException("属性值ID列表不能为空");
        }

        // 校验所有属性值是否属于该属性
        for (Long valueId : dto.getSelectedValueIds()) {
            AttributeValue attrValue = attributeValueMapper.selectById(valueId);
            if (attrValue == null) {
                throw new BusinessException("属性值不存在: " + valueId);
            }
            if (!attrValue.getAttrId().equals(entity.getAttrId())) {
                throw new BusinessException("属性值" + valueId + " 不属于该属性");
            }
        }

        // 检查被移除的属性值是否已被SKU绑定（即：旧值中有但新值中没有的，且已被SKU使用）
        List<Long> oldValueIds = JSON.parseArray(entity.getSelectedValues(), Long.class);
        List<Long> removedValueIds = oldValueIds.stream()
                .filter(vid -> !dto.getSelectedValueIds().contains(vid))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(removedValueIds)) {
            // 查出该SPU下未逻辑删除的SKU ID（@TableLogic自动过滤）
            List<Long> activeSkuIds = skuMapper.selectList(
                    new LambdaQueryWrapper<Sku>()
                            .eq(Sku::getSpuId, entity.getSpuId())
                            .select(Sku::getId)
            ).stream().map(Sku::getId).collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(activeSkuIds)) {
                for (Long removedValueId : removedValueIds) {
                    LambdaQueryWrapper<SkuSaleAttrValue> checkWrapper = new LambdaQueryWrapper<>();
                    checkWrapper.in(SkuSaleAttrValue::getSkuId, activeSkuIds);
                    checkWrapper.eq(SkuSaleAttrValue::getAttrValueId, removedValueId);
                    List<SkuSaleAttrValue> usedList = skuSaleAttrValueMapper.selectList(checkWrapper);
                    if (!CollectionUtils.isEmpty(usedList)) {
                        AttributeValue attrValue = attributeValueMapper.selectById(removedValueId);
                        String valueName = (attrValue != null) ? attrValue.getValue() : String.valueOf(removedValueId);
                        throw new BusinessException("属性值【" + valueName + "】已被SKU绑定，无法移除，请先删除相关SKU");
                    }
                }
            }
        }

        // 更新记录
        entity.setSelectedValues(JSON.toJSONString(dto.getSelectedValueIds()));
        entity.setUpdatedAt(LocalDateTime.now());

        int result = spuSaleAttrChoiceMapper.updateById(entity);
        log.info("商家 {} 更新SPU销售属性绑定 {} 成功", sellerId, id);

        return result > 0;
    }

    /**
     * 批量更新SPU销售属性绑定
     * <p>
     * 先校验所有更新项，如果任何一项存在"被移除的属性值已被SKU绑定"的情况，
     * 则全部失败回滚，并抛出异常告知前端。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateSaleAttr(List<SpuSaleAttrBindDto> dtoList, Long sellerId) {
        // 前置校验：遍历所有更新项，检查是否存在冲突
        for (SpuSaleAttrBindDto dto : dtoList) {
            // 查询原记录
            SpuSaleAttrChoice entity = spuSaleAttrChoiceMapper.selectById(dto.getId());
            if (entity == null) {
                throw new BusinessException("绑定记录不存在，ID: " + dto.getId());
            }

            // 校验权限
            checkSpuPermission(entity.getSpuId(), sellerId);

            // 校验属性值列表
            if (CollectionUtils.isEmpty(dto.getSelectedValueIds())) {
                throw new BusinessException("属性值ID列表不能为空，attrId: " + dto.getAttrId());
            }

            // 校验所有属性值是否属于该属性
            for (Long valueId : dto.getSelectedValueIds()) {
                AttributeValue attrValue = attributeValueMapper.selectById(valueId);
                if (attrValue == null) {
                    throw new BusinessException("属性值不存在: " + valueId);
                }
                if (!attrValue.getAttrId().equals(entity.getAttrId())) {
                    throw new BusinessException("属性值" + valueId + " 不属于该属性");
                }
            }

            // 检查被移除的属性值是否已被SKU绑定
            List<Long> oldValueIds = JSON.parseArray(entity.getSelectedValues(), Long.class);
            List<Long> removedValueIds = oldValueIds.stream()
                    .filter(vid -> !dto.getSelectedValueIds().contains(vid))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(removedValueIds)) {
                List<Long> activeSkuIds = skuMapper.selectList(
                        new LambdaQueryWrapper<Sku>()
                                .eq(Sku::getSpuId, entity.getSpuId())
                                .select(Sku::getId)
                ).stream().map(Sku::getId).collect(Collectors.toList());

                if (!CollectionUtils.isEmpty(activeSkuIds)) {
                    for (Long removedValueId : removedValueIds) {
                        LambdaQueryWrapper<SkuSaleAttrValue> checkWrapper = new LambdaQueryWrapper<>();
                        checkWrapper.in(SkuSaleAttrValue::getSkuId, activeSkuIds);
                        checkWrapper.eq(SkuSaleAttrValue::getAttrValueId, removedValueId);
                        List<SkuSaleAttrValue> usedList = skuSaleAttrValueMapper.selectList(checkWrapper);
                        if (!CollectionUtils.isEmpty(usedList)) {
                            AttributeValue attrValue = attributeValueMapper.selectById(removedValueId);
                            String valueName = (attrValue != null) ? attrValue.getValue() : String.valueOf(removedValueId);
                            throw new BusinessException("属性值【" + valueName + "】已被SKU绑定，无法移除，请先删除相关SKU");
                        }
                    }
                }
            }
        }

        // 所有校验通过后，逐一更新
        int count = 0;
        for (SpuSaleAttrBindDto dto : dtoList) {
            SpuSaleAttrChoice entity = spuSaleAttrChoiceMapper.selectById(dto.getId());
            entity.setSelectedValues(JSON.toJSONString(dto.getSelectedValueIds()));
            entity.setUpdatedAt(LocalDateTime.now());
            spuSaleAttrChoiceMapper.updateById(entity);
            count++;
            log.info("商家 {} 更新SPU销售属性绑定 {} 成功", sellerId, dto.getId());
        }

        return count;
    }

    /**
     * 删除SPU销售属性绑定
     *
     * @param id       绑定记录ID
     * @param sellerId 商家ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSaleAttr(Long id, Long sellerId) {
        // 查询原记录
        SpuSaleAttrChoice entity = spuSaleAttrChoiceMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("绑定记录不存在");
        }

        // 校验商家权限
        checkSpuPermission(entity.getSpuId(), sellerId);

        // 删除记录
        int result = spuSaleAttrChoiceMapper.deleteById(id);
        log.info("商家 {} 删除SPU销售属性绑定 {} 成功", sellerId, id);

        return result > 0;
    }

    /**
     * 获取SPU已绑定的销售属性列表
     *
     * @param spuId SPU ID
     * @return 销售属性绑定记录列表
     */
    @Override
    public List<SpuSaleAttrChoice> getSaleAttrsBySpuId(Long spuId) {
        LambdaQueryWrapper<SpuSaleAttrChoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpuSaleAttrChoice::getSpuId, spuId);
        return spuSaleAttrChoiceMapper.selectList(wrapper);
    }

    /**
     * 获取SPU销售属性详情列表（含属性值信息）
     *
     * @param spuId SPU ID
     * @return 销售属性详情VO列表
     */
    @Override
    public List<SpuAttrVo.SpuSaleAttrDetailVo> getSaleAttrsWithValuesBySpuId(Long spuId) {
        // 获取销售属性列表
        List<SpuSaleAttrChoice> saleAttrs = getSaleAttrsBySpuId(spuId);
        
        // 转换为包含属性值详情的VO列表
        return saleAttrs.stream().map(sa -> {
            SpuAttrVo.SpuSaleAttrDetailVo detailVo = new SpuAttrVo.SpuSaleAttrDetailVo();
            detailVo.setId(sa.getId());
            detailVo.setAttrId(sa.getAttrId());

            // 查询属性信息
            Attribute attr = attributeMapper.selectById(sa.getAttrId());
            if (attr != null) {
                detailVo.setAttrName(attr.getName());
            }

            // 获取属性值列表
            List<Long> valueIds = JSON.parseArray(sa.getSelectedValues(), Long.class);
            List<SpuAttrVo.AttrValueVo> valueVos = new ArrayList<>();
            if (!CollectionUtils.isEmpty(valueIds)) {
                for (Long valueId : valueIds) {
                    AttributeValue attrValue = attributeValueMapper.selectById(valueId);
                    if (attrValue != null) {
                        SpuAttrVo.AttrValueVo valueVo = new SpuAttrVo.AttrValueVo();
                        valueVo.setValueId(valueId);
                        valueVo.setValue(attrValue.getValue());
                        valueVo.setImageUrl(attrValue.getImageUrl());
                        valueVos.add(valueVo);
                    }
                }
            }
            detailVo.setSelectedValues(valueVos);
            return detailVo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取SPU所有属性（基本属性 + 销售属性，含详情）
     *
     * @param spuId SPU ID
     * @return SPU属性聚合VO
     */
    @Override
    public SpuAttrVo getAllAttrsBySpuId(Long spuId) {
        SpuAttrVo vo = new SpuAttrVo();

        // 获取SPU基本信息
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("SPU不存在");
        }
        vo.setSpuId(spuId);
        vo.setSpuName(spu.getName());

        // 获取基本属性详情
        List<SpuBasicAttrValue> basicAttrs = getBasicAttrsBySpuId(spuId);
        List<SpuAttrVo.SpuBasicAttrDetailVo> basicAttrVos = basicAttrs.stream().map(ba -> {
            SpuAttrVo.SpuBasicAttrDetailVo detailVo = new SpuAttrVo.SpuBasicAttrDetailVo();
            detailVo.setId(ba.getId());
            detailVo.setAttrId(ba.getAttrId());

            // 查询属性信息
            Attribute attr = attributeMapper.selectById(ba.getAttrId());
            if (attr != null) {
                detailVo.setAttrName(attr.getName());
                detailVo.setAttrType(attr.getAttrType());
            }

            detailVo.setAttrValueId(ba.getAttrValueId());

            // 查询属性值信息
            if (ba.getAttrValueId() != null) {
                AttributeValue attrValue = attributeValueMapper.selectById(ba.getAttrValueId());
                if (attrValue != null) {
                    detailVo.setAttrValue(attrValue.getValue());
                    detailVo.setImageUrl(attrValue.getImageUrl());
                }
            }

            detailVo.setManualValue(ba.getManualValue());
            return detailVo;
        }).collect(Collectors.toList());
        vo.setBasicAttrs(basicAttrVos);

        // 获取销售属性详情
        List<SpuSaleAttrChoice> saleAttrs = getSaleAttrsBySpuId(spuId);
        List<SpuAttrVo.SpuSaleAttrDetailVo> saleAttrVos = saleAttrs.stream().map(sa -> {
            SpuAttrVo.SpuSaleAttrDetailVo detailVo = new SpuAttrVo.SpuSaleAttrDetailVo();
            detailVo.setId(sa.getId());
            detailVo.setAttrId(sa.getAttrId());

            // 查询属性信息
            Attribute attr = attributeMapper.selectById(sa.getAttrId());
            if (attr != null) {
                detailVo.setAttrName(attr.getName());
            }

            List<Long> valueIds = JSON.parseArray(sa.getSelectedValues(), Long.class);
            List<SpuAttrVo.AttrValueVo> valueVos = new ArrayList<>();
            if (!CollectionUtils.isEmpty(valueIds)) {
                for (Long valueId : valueIds) {
                    AttributeValue attrValue = attributeValueMapper.selectById(valueId);
                    if (attrValue != null) {
                        SpuAttrVo.AttrValueVo valueVo = new SpuAttrVo.AttrValueVo();
                        valueVo.setValueId(valueId);
                        valueVo.setValue(attrValue.getValue());
                        valueVo.setImageUrl(attrValue.getImageUrl());
                        valueVos.add(valueVo);
                    }
                }
            }
            detailVo.setSelectedValues(valueVos);
            return detailVo;
        }).collect(Collectors.toList());
        vo.setSaleAttrs(saleAttrVos);

        return vo;
    }

    /**
     * 校验商家是否有权限操作该SPU
     *
     * @param spuId    SPU ID
     * @param sellerId 商家ID
     */
    private void checkSpuPermission(Long spuId, Long sellerId) {
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("SPU不存在");
        }
        if (!spu.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作该SPU");
        }
    }

    /**
     * 获取SPU可绑定的属性列表
     */
    @Override
    public SpuAvailableAttrVo getAvailableAttrsBySpuId(Long spuId) {
        log.info("获取SPU可绑定属性列表, spuId: {}", spuId);

        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("SPU不存在");
        }

        SpuAvailableAttrVo result = new SpuAvailableAttrVo();
        result.setSpuId(spuId);
        result.setCategoryId(spu.getCategoryId());

        // 获取分类下的所有属性
        List<Attribute> basicAttributes = attributeMapper.getByCategoryIdAndType(spu.getCategoryId(), Attribute.TYPE_BASIC);
        List<Attribute> saleAttributes = attributeMapper.getByCategoryIdAndType(spu.getCategoryId(), Attribute.TYPE_SALES);

        // 获取SPU已绑定的属性
        List<SpuBasicAttrValue> basicAttrs = getBasicAttrsBySpuId(spuId);
        List<SpuSaleAttrChoice> saleAttrs = getSaleAttrsBySpuId(spuId);

        // 构建已绑定属性的Map
        Map<Long, SpuBasicAttrValue> basicAttrMap = basicAttrs.stream()
                .collect(Collectors.toMap(SpuBasicAttrValue::getAttrId, a -> a, (a1, a2) -> a1));
        Map<Long, SpuSaleAttrChoice> saleAttrMap = saleAttrs.stream()
                .collect(Collectors.toMap(SpuSaleAttrChoice::getAttrId, a -> a, (a1, a2) -> a1));

        // 获取所有属性ID
        List<Long> allAttrIds = new ArrayList<>();
        basicAttributes.forEach(a -> allAttrIds.add(a.getId()));
        saleAttributes.forEach(a -> allAttrIds.add(a.getId()));

        //k-属性值id，v-属性值实体类
        Map<Long, List<AttributeValue>> attrValuesMap = new HashMap<>();
        if (!allAttrIds.isEmpty()) {
            List<AttributeValue> allAttrValues = attributeValueMapper.getByAttrIds(allAttrIds);
            attrValuesMap = allAttrValues.stream().collect(Collectors.groupingBy(AttributeValue::getAttrId));
        }

        // 构建基本属性列表
        List<SpuAvailableAttrVo.AvailableAttrItem> basicItems = buildAvailableAttrItems(
                basicAttributes, basicAttrMap, attrValuesMap, true
        );
        result.setBasicAttrs(basicItems);

        // 构建销售属性列表
        List<SpuAvailableAttrVo.AvailableAttrItem> saleItems = buildAvailableAttrItems(
                saleAttributes, saleAttrMap, attrValuesMap, false
        );
        result.setSaleAttrs(saleItems);

        return result;
    }

    /**
     * 构建可用属性项列表
     */
    private List<SpuAvailableAttrVo.AvailableAttrItem> buildAvailableAttrItems(
            List<Attribute> attributes,
            Map<Long, ?> boundMap,
            Map<Long, List<AttributeValue>> attrValuesMap,
            boolean isBasicAttr
    ) {
        List<SpuAvailableAttrVo.AvailableAttrItem> items = new ArrayList<>();

        for (Attribute attr : attributes) {
            SpuAvailableAttrVo.AvailableAttrItem item = new SpuAvailableAttrVo.AvailableAttrItem();
            item.setAttrId(attr.getId());
            item.setAttrName(attr.getName());
            item.setAttrType(attr.getAttrType());

            // 检查是否已绑定
            boolean bound = boundMap.containsKey(attr.getId());
            item.setBound(bound);

            if (bound) {
                if (isBasicAttr) {
                    SpuBasicAttrValue boundAttr = (SpuBasicAttrValue) boundMap.get(attr.getId());
                    item.setBoundId(boundAttr.getId());

                    // 设置当前绑定的值列表（基本属性：0 或 1 个）
                    List<Map<String, Object>> bCurrentValues = new ArrayList<>();
                    if (boundAttr.getAttrValueId() != null) {
                        AttributeValue attrValue = attributeValueMapper.selectById(boundAttr.getAttrValueId());
                        if (attrValue != null) {
                            Map<String, Object> cv = new HashMap<>();
                            cv.put("valueId", attrValue.getId());
                            cv.put("value", attrValue.getValue());
                            cv.put("imageUrl", attrValue.getImageUrl());
                            bCurrentValues.add(cv);
                        }
                    } else if (boundAttr.getManualValue() != null) {
                        Map<String, Object> cv = new HashMap<>();
                        cv.put("value", boundAttr.getManualValue());
                        bCurrentValues.add(cv);
                    }
                    item.setCurrentValues(bCurrentValues);
                } else {
                    SpuSaleAttrChoice boundAttr = (SpuSaleAttrChoice) boundMap.get(attr.getId());
                    item.setBoundId(boundAttr.getId());

                    // 设置当前绑定的值列表（销售属性：0 到 N 个）
                    List<Long> valueIds = JSON.parseArray(boundAttr.getSelectedValues(), Long.class);
                    List<Map<String, Object>> sCurrentValues = new ArrayList<>();
                    if (valueIds != null && !valueIds.isEmpty()) {
                        List<AttributeValue> allValues = attrValuesMap.getOrDefault(attr.getId(), new ArrayList<>());
                        Map<Long, AttributeValue> valueMap = allValues.stream()
                                .collect(Collectors.toMap(AttributeValue::getId, v -> v, (v1, v2) -> v1));
                        for (Long vid : valueIds) {
                            AttributeValue av = valueMap.get(vid);
                            if (av != null) {
                                Map<String, Object> cv = new HashMap<>();
                                cv.put("valueId", av.getId());
                                cv.put("value", av.getValue());
                                cv.put("imageUrl", av.getImageUrl());
                                sCurrentValues.add(cv);
                            }
                        }
                    }
                    item.setCurrentValues(sCurrentValues);
                }
            }

            // 设置可选属性值列表
            List<AttributeValue> attrValues = attrValuesMap.getOrDefault(attr.getId(), new ArrayList<>());
            List<Map<String, Object>> values = attrValues.stream().map(v -> {
                Map<String, Object> vm = new HashMap<>();
                vm.put("valueId", v.getId());
                vm.put("value", v.getValue());
                vm.put("imageUrl", v.getImageUrl());
                vm.put("sort", v.getSort());
                return vm;
            }).collect(Collectors.toList());
            item.setValues(values);

            items.add(item);
        }

        return items;
    }

    /**
     * 一次性为SPU绑定所有属性
     */
    //@Override
    //@Transactional(rollbackFor = Exception.class)
    //public Map<String, Object> bindAllAttrs(SpuAttrFullBindDto dto, Long sellerId) {
    //    log.info("一次性绑定SPU所有属性, spuId: {}", dto.getSpuId());
    //
    //    // 校验权限
    //    checkSpuPermission(dto.getSpuId(), sellerId);
    //
    //    Map<String, Object> result = new HashMap<>();
    //
    //    // 删除SPU下所有SKU的销售属性绑定
    //    List<Sku> skus = skuMapper.selectList(
    //            new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, dto.getSpuId()));
    //    for (Sku sku : skus) {
    //        skuSaleAttrValueMapper.delete(
    //                new LambdaQueryWrapper<SkuSaleAttrValue>().eq(SkuSaleAttrValue::getSkuId, sku.getId()));
    //    }
    //
    //    // 删除SPU下所有SKU
    //    skuMapper.delete(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, dto.getSpuId()));
    //
    //    // 清除原有的绑定
    //    int deletedBasicCount = 0;
    //    int deletedSaleCount = 0;
    //    LambdaQueryWrapper<SpuBasicAttrValue> deleteBasicWrapper = new LambdaQueryWrapper<>();
    //    deleteBasicWrapper.eq(SpuBasicAttrValue::getSpuId, dto.getSpuId());
    //    deletedBasicCount = spuBasicAttrValueMapper.delete(deleteBasicWrapper);
    //
    //    LambdaQueryWrapper<SpuSaleAttrChoice> deleteSaleWrapper = new LambdaQueryWrapper<>();
    //    deleteSaleWrapper.eq(SpuSaleAttrChoice::getSpuId, dto.getSpuId());
    //    deletedSaleCount = spuSaleAttrChoiceMapper.delete(deleteSaleWrapper);
    //
    //    result.put("deletedBasicCount", deletedBasicCount);
    //    result.put("deletedSaleCount", deletedSaleCount);
    //
    //    int boundBasicCount = 0;
    //    int boundSaleCount = 0;
    //
    //    // 绑定基本属性
    //    if (!CollectionUtils.isEmpty(dto.getBasicAttrs())) {
    //        for (SpuBasicAttrBindDto basicDto : dto.getBasicAttrs()) {
    //            basicDto.setSpuId(dto.getSpuId());
    //            try {
    //                bindBasicAttr(basicDto, sellerId);
    //                boundBasicCount++;
    //            } catch (Exception e) {
    //                log.warn("绑定基本属性失败, attrId: {}, error: {}", basicDto.getAttrId(), e.getMessage());
    //            }
    //        }
    //    }
    //
    //    // 绑定销售属性
    //    if (!CollectionUtils.isEmpty(dto.getSaleAttrs())) {
    //        for (SpuSaleAttrBindDto saleDto : dto.getSaleAttrs()) {
    //            saleDto.setSpuId(dto.getSpuId());
    //            try {
    //                bindSaleAttr(saleDto, sellerId);
    //                boundSaleCount++;
    //            } catch (Exception e) {
    //                log.warn("绑定销售属性失败, attrId: {}, error: {}", saleDto.getAttrId(), e.getMessage());
    //            }
    //        }
    //    }
    //
    //    result.put("boundBasicCount", boundBasicCount);
    //    result.put("boundSaleCount", boundSaleCount);
    //    result.put("spuId", dto.getSpuId());
    //
    //    log.info("SPU属性绑定完成, 绑定基本属性: {}, 绑定销售属性: {}", boundBasicCount, boundSaleCount);
    //
    //    return result;
    //}
}