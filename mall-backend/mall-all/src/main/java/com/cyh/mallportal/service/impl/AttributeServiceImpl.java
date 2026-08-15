package com.cyh.mallportal.service.impl;

import com.cyh.mallportal.entity.Attribute;
import com.cyh.mallportal.entity.AttributeValue;
import com.cyh.mallportal.mapper.AttributeMapper;
import com.cyh.mallportal.mapper.AttributeValueMapper;
import com.cyh.mallportal.service.AttributeService;
import com.cyh.mallportal.vo.AttrValueVo;
import com.cyh.mallportal.vo.CategoryAttrVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 属性Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttributeServiceImpl implements AttributeService {

    private final AttributeMapper attributeMapper;
    private final AttributeValueMapper attributeValueMapper;

    /**
     * 根据分类ID获取该分类下的所有属性
     */
    @Override
    public List<Attribute> getByCategoryId(Long categoryId) {
        log.info("获取分类属性列表, categoryId: {}", categoryId);
        return attributeMapper.getByCategoryId(categoryId);
    }

    /**
     * 根据分类ID和属性类型获取属性列表
     */
    @Override
    public List<Attribute> getByCategoryIdAndType(Long categoryId, Integer attrType) {
        log.info("获取分类属性列表, categoryId: {}, attrType: {}", categoryId, attrType);
        return attributeMapper.getByCategoryIdAndType(categoryId, attrType);
    }

    /**
     * 根据属性ID获取属性值列表
     */
    @Override
    public List<AttributeValue> getValuesByAttrId(Long attrId) {
        log.info("获取属性值列表, attrId: {}", attrId);
        return attributeValueMapper.getByAttrId(attrId);
    }

    /**
     * 获取分类下的销售属性及其属性值
     */
    @Override
    public List<CategoryAttrVo> getSalesAttributesWithValues(Long categoryId) {
        log.info("获取分类销售属性及属性值, categoryId: {}", categoryId);

        List<Attribute> attributes = attributeMapper.getByCategoryIdAndType(categoryId, Attribute.TYPE_SALES);
        return buildAttributeWithValues(attributes);
    }

    /**
     * 获取分类下的所有属性及其属性值（含销售属性和基本属性）
     */
    @Override
    public List<CategoryAttrVo> getCategoryAttributesWithValues(Long categoryId) {
        log.info("获取分类所有属性及属性值, categoryId: {}", categoryId);

        List<Attribute> attributes = attributeMapper.getByCategoryId(categoryId);
        return buildAttributeWithValues(attributes);
    }

    /**
     * 获取分类下的基本属性及其属性值
     */
    @Override
    public List<CategoryAttrVo> getBasicAttributesWithValues(Long categoryId) {
        log.info("获取分类基本属性及属性值, categoryId: {}", categoryId);

        List<Attribute> attributes = attributeMapper.getByCategoryIdAndType(categoryId, Attribute.TYPE_BASIC);
        return buildAttributeWithValues(attributes);
    }

    /**
     * 构建属性及属性值 VO 列表
     */
    private List<CategoryAttrVo> buildAttributeWithValues(List<Attribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有属性ID
        List<Long> attrIds = attributes.stream()
                .map(Attribute::getId)
                .collect(Collectors.toList());

        // 获取所有属性值
        List<AttributeValue> allValues = attributeValueMapper.getByAttrIds(attrIds);
        Map<Long, List<AttributeValue>> valueMap = allValues.stream()
                .collect(Collectors.groupingBy(AttributeValue::getAttrId));

        // 构建结果
        List<CategoryAttrVo> result = new ArrayList<>();
        for (Attribute attr : attributes) {
            CategoryAttrVo vo = new CategoryAttrVo();
            vo.setAttrId(attr.getId());
            vo.setAttrName(attr.getName());
            vo.setAttrType(attr.getAttrType());
            vo.setSort(attr.getSort());

            // 获取该属性的属性值
            List<AttributeValue> values = valueMap.getOrDefault(attr.getId(), new ArrayList<>());
            List<AttrValueVo> valueVos = values.stream()
                    .map(v -> {
                        AttrValueVo valueVo = new AttrValueVo();
                        valueVo.setValueId(v.getId());
                        valueVo.setValue(v.getValue());
                        valueVo.setImageUrl(v.getImageUrl());
                        valueVo.setSort(v.getSort());
                        return valueVo;
                    })
                    .collect(Collectors.toList());
            vo.setValues(valueVos);

            result.add(vo);
        }

        return result;
    }
}
