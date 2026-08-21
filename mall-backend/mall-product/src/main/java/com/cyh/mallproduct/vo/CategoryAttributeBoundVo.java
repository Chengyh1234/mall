package com.cyh.mallproduct.vo;

import lombok.Data;

/**
 * 分类-属性绑定视图对象
 *
 * 展示分类已绑定的属性列表，附带该属性是否被该分类下的 SPU 引用的状态
 */
@Data
public class CategoryAttributeBoundVo {

    /**
     * category_attributes 表主键ID
     */
    private Long id;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 属性ID
     */
    private Long attrId;

    /**
     * 属性名称
     */
    private String attrName;

    /**
     * 属性类型：1=销售属性，2=基本属性
     */
    private Integer attrType;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 该分类下的 SPU 是否已使用了该属性
     * true=已被引用，禁止解绑；false=未被引用，可以解绑
     */
    private Boolean hasSpuUsage;
}