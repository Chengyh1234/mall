package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallportal.dto.AttributeDto;
import com.cyh.mallportal.dto.AttributeValueDto;
import com.cyh.mallportal.entity.Attribute;
import com.cyh.mallportal.entity.AttributeValue;
import com.cyh.mallportal.mapper.AttributeMapper;
import com.cyh.mallportal.mapper.AttributeValueMapper;
import com.cyh.mallportal.service.AttributeManageService;
import com.cyh.mallportal.vo.AttrValueVo;
import com.cyh.mallportal.vo.AttributeVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 属性管理 Service 实现类
 *
 * 提供属性的增删改查及属性值的增删改查功能，
 * 包含业务校验逻辑（如引用检查、类型变更限制等）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttributeManageServiceImpl implements AttributeManageService {

    private final AttributeMapper attributeMapper;
    private final AttributeValueMapper attributeValueMapper;

    // ==================== 属性管理 ====================

    @Override
    public Long addAttribute(AttributeDto dto) {
        log.info("新增属性: name={}, attrType={}", dto.getName(), dto.getAttrType());

        Attribute attribute = new Attribute();
        attribute.setName(dto.getName());
        attribute.setAttrType(dto.getAttrType());
        attribute.setSort(dto.getSort() != null ? dto.getSort() : 0);

        attributeMapper.insert(attribute);
        log.info("属性新增成功, id={}", attribute.getId());
        return attribute.getId();
    }

    @Override
    public boolean updateAttribute(Long id, AttributeDto dto) {
        log.info("修改属性: id={}, name={}, attrType={}", id, dto.getName(), dto.getAttrType());

        // 检查属性是否存在
        Attribute existing = attributeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("属性不存在");
        }

        // 如果该属性下已有属性值，则禁止修改属性类型
        if (dto.getAttrType() != null && !dto.getAttrType().equals(existing.getAttrType())) {
            Long valueCount = attributeValueMapper.selectCount(
                    new LambdaQueryWrapper<AttributeValue>().eq(AttributeValue::getAttrId, id)
            );
            if (valueCount > 0) {
                throw new BusinessException("该属性下存在属性值，无法修改属性类型");
            }
        }

        // 更新属性信息
        Attribute update = new Attribute();
        update.setId(id);
        update.setName(dto.getName());
        update.setAttrType(dto.getAttrType());
        update.setSort(dto.getSort() != null ? dto.getSort() : existing.getSort());

        int rows = attributeMapper.updateById(update);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAttribute(Long id) {
        log.info("删除属性: id={}", id);

        // 检查属性是否存在
        Attribute existing = attributeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("属性不存在");
        }

        // 检查该属性下是否有属性值，有则禁止删除
        long valueCount = attributeValueMapper.selectCount(
                new LambdaQueryWrapper<AttributeValue>().eq(AttributeValue::getAttrId, id)
        );
        if (valueCount > 0) {
            throw new BusinessException("该属性下存在属性值，请先删除所有属性值");
        }

        // 执行删除（category_attributes 的关联由数据库 ON DELETE CASCADE 自动处理）
        int rows = attributeMapper.deleteById(id);
        return rows > 0;
    }

    @Override
    public List<AttributeVo> listAllAttributes() {
        log.info("查询全部属性列表（含属性值）");

        // 查询所有属性
        List<Attribute> attributes = attributeMapper.selectList(
                new LambdaQueryWrapper<Attribute>().orderByAsc(Attribute::getSort)
        );

        // 查询所有属性值，按属性ID分组
        List<AttributeValue> allValues = attributeValueMapper.selectList(
                new LambdaQueryWrapper<AttributeValue>().orderByAsc(AttributeValue::getSort)
        );
        var valueMap = allValues.stream()
                .collect(Collectors.groupingBy(AttributeValue::getAttrId));

        // 组装 VO
        List<AttributeVo> result = new ArrayList<>();
        for (Attribute attr : attributes) {
            AttributeVo vo = buildAttributeVo(attr);
            List<AttributeValue> attrValues = valueMap.getOrDefault(attr.getId(), new ArrayList<>());
            vo.setValues(attrValues.stream().map(this::toAttrValueVo).collect(Collectors.toList()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public AttributeVo getAttributeDetail(Long id) {
        log.info("查询属性详情: id={}", id);

        Attribute attribute = attributeMapper.selectById(id);
        if (attribute == null) {
            throw new BusinessException("属性不存在");
        }

        List<AttributeValue> values = attributeValueMapper.selectList(
                new LambdaQueryWrapper<AttributeValue>()
                        .eq(AttributeValue::getAttrId, id)
                        .orderByAsc(AttributeValue::getSort)
        );

        AttributeVo vo = buildAttributeVo(attribute);
        vo.setValues(values.stream().map(this::toAttrValueVo).collect(Collectors.toList()));
        return vo;
    }

    // ==================== 属性值管理 ====================

    @Override
    public Long addAttributeValue(AttributeValueDto dto) {
        log.info("新增属性值: attrId={}, value={}", dto.getAttrId(), dto.getValue());

        // 检查所属属性是否存在
        Attribute attribute = attributeMapper.selectById(dto.getAttrId());
        if (attribute == null) {
            throw new BusinessException("所属属性不存在");
        }

        AttributeValue attributeValue = new AttributeValue();
        attributeValue.setAttrId(dto.getAttrId());
        attributeValue.setValue(dto.getValue());
        attributeValue.setImageUrl(dto.getImageUrl());
        attributeValue.setSort(dto.getSort() != null ? dto.getSort() : 0);

        attributeValueMapper.insert(attributeValue);
        log.info("属性值新增成功, id={}", attributeValue.getId());
        return attributeValue.getId();
    }

    @Override
    public boolean updateAttributeValue(Long id, AttributeValueDto dto) {
        log.info("修改属性值: id={}, value={}", id, dto.getValue());

        // 检查属性值是否存在
        AttributeValue existing = attributeValueMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("属性值不存在");
        }

        // 检查该属性值是否被 SPU 或 SKU 引用
        checkAttributeValueUsed(id);

        // 更新属性值信息
        AttributeValue update = new AttributeValue();
        update.setId(id);
        update.setValue(dto.getValue());
        update.setImageUrl(dto.getImageUrl());
        update.setSort(dto.getSort() != null ? dto.getSort() : existing.getSort());

        int rows = attributeValueMapper.updateById(update);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAttributeValue(Long id) {
        log.info("删除属性值: id={}", id);

        // 检查属性值是否存在
        AttributeValue existing = attributeValueMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("属性值不存在");
        }

        // 检查该属性值是否被 SPU 或 SKU 引用
        checkAttributeValueUsed(id);

        // 执行删除
        int rows = attributeValueMapper.deleteById(id);
        return rows > 0;
    }

    @Override
    public List<AttrValueVo> listAttributeValues(Long attrId) {
        log.info("查询属性值列表: attrId={}", attrId);

        // 检查所属属性是否存在
        Attribute attribute = attributeMapper.selectById(attrId);
        if (attribute == null) {
            throw new BusinessException("属性不存在");
        }

        return attributeValueMapper.selectList(
                new LambdaQueryWrapper<AttributeValue>()
                        .eq(AttributeValue::getAttrId, attrId)
                        .orderByAsc(AttributeValue::getSort)
        ).stream().map(this::toAttrValueVo).collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    /**
     * 检查属性值是否被 SPU 或 SKU 引用
     * 如果被引用则抛出 BusinessException
     *
     * @param attrValueId 属性值ID
     */
    private void checkAttributeValueUsed(Long attrValueId) {
        int spuBasicCount = attributeValueMapper.countUsedInSpuBasic(attrValueId);
        if (spuBasicCount > 0) {
            throw new BusinessException("该属性值已被 SPU 基本属性引用，无法修改或删除");
        }

        int spuSaleCount = attributeValueMapper.countUsedInSpuSale(attrValueId);
        if (spuSaleCount > 0) {
            throw new BusinessException("该属性值已被 SPU 销售属性引用，无法修改或删除");
        }

        int skuSaleCount = attributeValueMapper.countUsedInSkuSale(attrValueId);
        if (skuSaleCount > 0) {
            throw new BusinessException("该属性值已被 SKU 销售属性引用，无法修改或删除");
        }
    }

    /**
     * 将 Attribute 实体转换为 AttributeVo
     */
    private AttributeVo buildAttributeVo(Attribute attribute) {
        AttributeVo vo = new AttributeVo();
        vo.setId(attribute.getId());
        vo.setName(attribute.getName());
        vo.setAttrType(attribute.getAttrType());
        vo.setSort(attribute.getSort());
        vo.setCreatedAt(attribute.getCreatedAt());
        vo.setUpdatedAt(attribute.getUpdatedAt());
        return vo;
    }

    /**
     * 将 AttributeValue 实体转换为 AttrValueVo
     */
    private AttrValueVo toAttrValueVo(AttributeValue attrValue) {
        AttrValueVo vo = new AttrValueVo();
        vo.setValueId(attrValue.getId());
        vo.setValue(attrValue.getValue());
        vo.setImageUrl(attrValue.getImageUrl());
        vo.setSort(attrValue.getSort());
        return vo;
    }
}