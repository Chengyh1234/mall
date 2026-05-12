package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.entity.Attribute;
import com.cyh.mallportal.service.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 属性管理控制器
 * 提供属性相关的API接口
 */
@RestController
@RequestMapping("/attribute")
public class AttributeController {

    @Autowired
    private AttributeService attributeService;

    /**
     * 获取分类下的所有属性
     *
     * @param categoryId 分类ID
     * @return 属性列表
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<Attribute>> getByCategory(@PathVariable Long categoryId) {
        List<Attribute> attributes = attributeService.getByCategoryId(categoryId);
        return Result.success(attributes);
    }

    /**
     * 获取分类下的销售属性及其属性值
     *
     * @param categoryId 分类ID
     * @return 销售属性及属性值列表
     */
    @GetMapping("/sales/{categoryId}")
    public Result<List<Map<String, Object>>> getSalesAttributes(@PathVariable Long categoryId) {
        List<Map<String, Object>> attributes = attributeService.getSalesAttributesWithValues(categoryId);
        return Result.success(attributes);
    }

    /**
     * 获取分类下的基本属性及其属性值
     *
     * @param categoryId 分类ID
     * @return 基本属性及属性值列表
     */
    @GetMapping("/basic/{categoryId}")
    public Result<List<Map<String, Object>>> getBasicAttributes(@PathVariable Long categoryId) {
        List<Map<String, Object>> attributes = attributeService.getBasicAttributesWithValues(categoryId);
        return Result.success(attributes);
    }

    /**
     * 获取分类下指定类型的属性及其属性值
     *
     * @param categoryId 分类ID
     * @param type       属性类型（1=销售属性，2=基本属性）
     * @return 属性及属性值列表
     */
    @GetMapping("/category/{categoryId}/type/{type}")
    public Result<List<Map<String, Object>>> getAttributesByType(@PathVariable Long categoryId,
                                                                @PathVariable Integer type) {
        List<Map<String, Object>> attributes;
        if (type == Attribute.TYPE_SALES) {
            attributes = attributeService.getSalesAttributesWithValues(categoryId);
        } else if (type == Attribute.TYPE_BASIC) {
            attributes = attributeService.getBasicAttributesWithValues(categoryId);
        } else {
            return Result.error("无效的属性类型");
        }
        return Result.success(attributes);
    }

    /**
     * 获取SPU的基本属性值
     *
     * @param spuId SPU ID
     * @return SPU基本属性值列表
     */
    @GetMapping("/spu/{spuId}/basic")
    public Result<List<Map<String, Object>>> getSpuBasicAttributes(@PathVariable Long spuId) {
        List<Map<String, Object>> attributes = attributeService.getSpuBasicAttributes(spuId);
        return Result.success(attributes);
    }

    /**
     * 获取SPU的销售属性选择
     *
     * @param spuId SPU ID
     * @return SPU销售属性选择列表
     */
    @GetMapping("/spu/{spuId}/sales")
    public Result<List<Map<String, Object>>> getSpuSaleAttributes(@PathVariable Long spuId) {
        List<Map<String, Object>> attributes = attributeService.getSpuSaleAttributes(spuId);
        return Result.success(attributes);
    }

    /**
     * 获取SPU的所有属性（基本属性+销售属性）
     *
     * @param spuId SPU ID
     * @return SPU所有属性
     */
    @GetMapping("/spu/{spuId}/all")
    public Result<Map<String, Object>> getSpuAllAttributes(@PathVariable Long spuId) {
        List<Map<String, Object>> basicAttributes = attributeService.getSpuBasicAttributes(spuId);
        List<Map<String, Object>> saleAttributes = attributeService.getSpuSaleAttributes(spuId);

        Map<String, Object> result = new HashMap<>();
        result.put("basicAttributes", basicAttributes);
        result.put("saleAttributes", saleAttributes);

        return Result.success(result);
    }
}
