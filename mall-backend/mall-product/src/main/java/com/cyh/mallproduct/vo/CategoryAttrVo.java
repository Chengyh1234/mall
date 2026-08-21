package com.cyh.mallproduct.vo;

import lombok.Data;

import java.util.List;

/**
 * 分类属性 VO（商户端）
 *
 * 展示某个分类下的属性及其可选属性值，用于商户发布/编辑商品时选择属性
 * 对应接口：
 *   GET /attribute/category/{categoryId}
 *   GET /attribute/basic/{categoryId}
 *   GET /attribute/{categoryId}/type/{type}
 */
@Data
public class CategoryAttrVo {

    /**
     * 属性ID
     */
    private Long attrId;

    /**
     * 属性名称（如：颜色、内存、屏幕尺寸）
     */
    private String attrName;

    /**
     * 属性类型：1=销售属性，2=基本属性
     */
    private Integer attrType;

    /**
     * 排序（越小越靠前）
     */
    private Integer sort;

    /**
     * 该属性下的可选属性值列表
     */
    private List<AttrValueVo> values;
}