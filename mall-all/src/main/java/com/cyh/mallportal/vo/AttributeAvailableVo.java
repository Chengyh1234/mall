package com.cyh.mallportal.vo;

import lombok.Data;

/**
 * 可绑定属性 VO（管理后台）
 *
 * 展示分类尚未绑定的属性列表，供运营管理员选择绑定
 * 对应接口：
 *   GET /category/attribute/manage/available/{categoryId}
 */
@Data
public class AttributeAvailableVo {

    /**
     * 属性ID
     */
    private Long id;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 属性类型：1-销售属性，2-基本属性
     */
    private Integer attrType;

    /**
     * 排序
     */
    private Integer sort;
}