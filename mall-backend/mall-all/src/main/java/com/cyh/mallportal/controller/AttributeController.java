package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.entity.Attribute;
import com.cyh.mallportal.service.AttributeService;
import com.cyh.mallportal.vo.CategoryAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 属性管理控制器 以及处理响应
 * 提供属性相关的API接口
 */
@RestController
@RequestMapping("/attribute")
public class AttributeController {

    @Autowired
    private AttributeService attributeService;

    /**
     * 获取分类下的所有属性及其属性值（含销售属性和基本属性）
     *
     * @param categoryId 分类ID
     * @return 属性及属性值列表
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<CategoryAttrVo>> getByCategory(@PathVariable Long categoryId) {
        List<CategoryAttrVo> attributes = attributeService.getCategoryAttributesWithValues(categoryId);
        return Result.success(attributes);
    }

    /**
     * 获取分类下的销售属性及其属性值
     *
     * @param categoryId 分类ID
     * @return 销售属性及属性值列表
     */
    @GetMapping("/sales/{categoryId}")
    public Result<List<CategoryAttrVo>> getSalesAttributes(@PathVariable Long categoryId) {
        List<CategoryAttrVo> attributes = attributeService.getSalesAttributesWithValues(categoryId);
        return Result.success(attributes);
    }

    /**
     * 获取分类下的基本属性及其属性值
     *
     * @param categoryId 分类ID
     * @return 基本属性及属性值列表
     */
    @GetMapping("/basic/{categoryId}")
    public Result<List<CategoryAttrVo>> getBasicAttributes(@PathVariable Long categoryId) {
        List<CategoryAttrVo> attributes = attributeService.getBasicAttributesWithValues(categoryId);
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
    public Result<List<CategoryAttrVo>> getAttributesByType(@PathVariable Long categoryId,
                                                                @PathVariable Integer type) {
        List<CategoryAttrVo> attributes;
        if (type == Attribute.TYPE_SALES) {
            attributes = attributeService.getSalesAttributesWithValues(categoryId);
        } else if (type == Attribute.TYPE_BASIC) {
            attributes = attributeService.getBasicAttributesWithValues(categoryId);
        } else {
            return Result.error("无效的属性类型");
        }
        return Result.success(attributes);
    }

    }
