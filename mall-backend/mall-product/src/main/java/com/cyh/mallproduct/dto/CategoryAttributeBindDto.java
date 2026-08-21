package com.cyh.mallproduct.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分类-属性绑定请求 DTO
 *
 * 用于运营管理员和超级管理员将属性绑定到分类
 */
@Data
public class CategoryAttributeBindDto {

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 属性ID
     */
    @NotNull(message = "属性ID不能为空")
    private Long attrId;

    /**
     * 排序（越小越靠前，默认0）
     */
    private Integer sort;
}