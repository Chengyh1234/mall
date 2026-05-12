package com.cyh.mallportal.service.impl;

import com.cyh.mallportal.entity.Attribute;
import com.cyh.mallportal.entity.AttributeValue;
import com.cyh.mallportal.entity.SpuBasicAttrValue;
import com.cyh.mallportal.entity.SpuSaleAttrChoice;
import com.cyh.mallportal.mapper.AttributeMapper;
import com.cyh.mallportal.mapper.AttributeValueMapper;
import com.cyh.mallportal.mapper.SpuBasicAttrValueMapper;
import com.cyh.mallportal.mapper.SpuSaleAttrChoiceMapper;
import com.cyh.mallportal.service.AttributeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
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
    private final SpuBasicAttrValueMapper spuBasicAttrValueMapper;
    private final SpuSaleAttrChoiceMapper spuSaleAttrChoiceMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public List<Map<String, Object>> getSalesAttributesWithValues(Long categoryId) {
        log.info("获取分类销售属性及属性值, categoryId: {}", categoryId);

        List<Attribute> attributes = attributeMapper.getByCategoryIdAndType(categoryId, Attribute.TYPE_SALES);
        return buildAttributeWithValues(attributes);
    }

    /**
     * 获取分类下的基本属性及其属性值
     */
    @Override
    public List<Map<String, Object>> getBasicAttributesWithValues(Long categoryId) {
        log.info("获取分类基本属性及属性值, categoryId: {}", categoryId);

        List<Attribute> attributes = attributeMapper.getByCategoryIdAndType(categoryId, Attribute.TYPE_BASIC);
        return buildAttributeWithValues(attributes);
    }

    /**
     * 获取SPU的基本属性值
     */
    @Override
    public List<Map<String, Object>> getSpuBasicAttributes(Long spuId) {
        log.info("获取SPU基本属性值, spuId: {}", spuId);

        List<SpuBasicAttrValue> spuBasicAttrValues = spuBasicAttrValueMapper.getBySpuId(spuId);
        if (spuBasicAttrValues == null || spuBasicAttrValues.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有属性ID
        List<Long> attrIds = spuBasicAttrValues.stream()
                .map(SpuBasicAttrValue::getAttrId)
                .distinct()
                .collect(Collectors.toList());

        // 获取属性信息
        List<Attribute> attributes = attributeMapper.selectBatchIds(attrIds);
        Map<Long, Attribute> attrMap = attributes.stream()
                .collect(Collectors.toMap(Attribute::getId, a -> a));

        // 获取所有属性值ID
        List<Long> attrValueIds = spuBasicAttrValues.stream()
                .filter(v -> v.getAttrValueId() != null)
                .map(SpuBasicAttrValue::getAttrValueId)
                .distinct()
                .collect(Collectors.toList());

        // 获取属性值信息
        Map<Long, AttributeValue> attrValueMap = new HashMap<>();
        if (!attrValueIds.isEmpty()) {
            List<AttributeValue> attrValues = attributeValueMapper.selectBatchIds(attrValueIds);
            attrValueMap = attrValues.stream()
                    .collect(Collectors.toMap(AttributeValue::getId, v -> v));
        }

        // 构建结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (SpuBasicAttrValue spuValue : spuBasicAttrValues) {
            Attribute attr = attrMap.get(spuValue.getAttrId());
            if (attr == null) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("attrId", attr.getId());
            map.put("attrName", attr.getName());
            map.put("attrType", attr.getAttrType());

            // 获取属性值
            if (spuValue.getAttrValueId() != null) {
                AttributeValue attrValue = attrValueMap.get(spuValue.getAttrValueId());
                if (attrValue != null) {
                    map.put("value", attrValue.getValue());
                    map.put("imageUrl", attrValue.getImageUrl());
                }
            } else if (spuValue.getManualValue() != null) {
                map.put("value", spuValue.getManualValue());
            }

            result.add(map);
        }

        return result;
    }

    /**
     * 获取SPU的销售属性选择
     */
    @Override
    public List<Map<String, Object>> getSpuSaleAttributes(Long spuId) {
        log.info("获取SPU销售属性选择, spuId: {}", spuId);

        List<SpuSaleAttrChoice> choices = spuSaleAttrChoiceMapper.getBySpuId(spuId);
        if (choices == null || choices.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有属性ID
        List<Long> attrIds = choices.stream()
                .map(SpuSaleAttrChoice::getAttrId)
                .distinct()
                .collect(Collectors.toList());

        // 获取属性信息
        List<Attribute> attributes = attributeMapper.selectBatchIds(attrIds);
        Map<Long, Attribute> attrMap = attributes.stream()
                .collect(Collectors.toMap(Attribute::getId, a -> a));

        // 获取所有属性值ID
        final List<Long> allAttrValueIds = new ArrayList<>();
        for (SpuSaleAttrChoice choice : choices) {
            JsonNode selectedValues = parseJson(choice.getSelectedValues());
            if (selectedValues != null && selectedValues.isArray()) {
                selectedValues.forEach(node -> {
                    allAttrValueIds.add(node.asLong());
                });
            }
        }

        // 获取属性值信息
        final Map<Long, AttributeValue> attrValueMap = new HashMap<>();
        if (!allAttrValueIds.isEmpty()) {
            List<AttributeValue> attrValues = attributeValueMapper.selectBatchIds(allAttrValueIds);
            attrValueMap.putAll(attrValues.stream()
                    .collect(Collectors.toMap(AttributeValue::getId, v -> v)));
        }

        // 构建结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (SpuSaleAttrChoice choice : choices) {
            Attribute attr = attrMap.get(choice.getAttrId());
            if (attr == null) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("attrId", attr.getId());
            map.put("attrName", attr.getName());

            // 获取选中的属性值
            final List<Map<String, Object>> values = new ArrayList<>();
            JsonNode selectedValues = parseJson(choice.getSelectedValues());
            if (selectedValues != null && selectedValues.isArray()) {
                selectedValues.forEach(node -> {
                    Long valueId = node.asLong();
                    final AttributeValue attrValue = attrValueMap.get(valueId);
                    if (attrValue != null) {
                        Map<String, Object> valueMap = new HashMap<>();
                        valueMap.put("valueId", attrValue.getId());
                        valueMap.put("value", attrValue.getValue());
                        valueMap.put("imageUrl", attrValue.getImageUrl());
                        values.add(valueMap);
                    }
                });
            }
            map.put("values", values);

            result.add(map);
        }

        return result;
    }

    /**
     * 解析JSON字符串为JsonNode
     */
    private JsonNode parseJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(jsonStr);
        } catch (JsonProcessingException e) {
            log.error("解析JSON字符串失败: {}", jsonStr, e);
            return null;
        }
    }

    /**
     * 构建属性及属性值列表
     */
    private List<Map<String, Object>> buildAttributeWithValues(List<Attribute> attributes) {
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
        List<Map<String, Object>> result = new ArrayList<>();
        for (Attribute attr : attributes) {
            Map<String, Object> map = new HashMap<>();
            map.put("attrId", attr.getId());
            map.put("attrName", attr.getName());
            map.put("attrType", attr.getAttrType());
            map.put("sort", attr.getSort());

            // 获取该属性的属性值
            List<AttributeValue> values = valueMap.getOrDefault(attr.getId(), new ArrayList<>());
            List<Map<String, Object>> valueList = values.stream()
                    .map(v -> {
                        Map<String, Object> vm = new HashMap<>();
                        vm.put("valueId", v.getId());
                        vm.put("value", v.getValue());
                        vm.put("imageUrl", v.getImageUrl());
                        vm.put("sort", v.getSort());
                        return vm;
                    })
                    .collect(Collectors.toList());
            map.put("values", valueList);

            result.add(map);
        }

        return result;
    }
}
