package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.Attribute;
import com.cyh.mallportal.entity.AttributeValue;

import java.util.List;
import java.util.Map;

/**
 * 属性Service接口
 */
public interface AttributeService {

    /**
     * 根据分类ID获取该分类下的所有属性
     *
     * @param categoryId 分类ID
     * @return 属性列表
     */
    List<Attribute> getByCategoryId(Long categoryId);

    /**
     * 根据分类ID和属性类型获取属性列表
     *
     * @param categoryId 分类ID
     * @param attrType   属性类型（1=销售属性，2=基本属性）
     * @return 属性列表
     */
    List<Attribute> getByCategoryIdAndType(Long categoryId, Integer attrType);

    /**
     * 根据属性ID获取属性值列表
     *
     * @param attrId 属性ID
     * @return 属性值列表
     */
    List<AttributeValue> getValuesByAttrId(Long attrId);

    /**
     * 获取分类下的销售属性及其属性值
     *
     * @param categoryId 分类ID
     * @return 销售属性及属性值列表
     */
    List<Map<String, Object>> getSalesAttributesWithValues(Long categoryId);

    /**
     * 获取分类下的基本属性及其属性值
     *
     * @param categoryId 分类ID
     * @return 基本属性及属性值列表
     */
    List<Map<String, Object>> getBasicAttributesWithValues(Long categoryId);

    /**
     * 获取SPU的基本属性值
     *
     * @param spuId SPU ID
     * @return SPU基本属性值列表
     */
    List<Map<String, Object>> getSpuBasicAttributes(Long spuId);

    /**
     * 获取SPU的销售属性选择
     *
     * @param spuId SPU ID
     * @return SPU销售属性选择列表
     */
    List<Map<String, Object>> getSpuSaleAttributes(Long spuId);
}
