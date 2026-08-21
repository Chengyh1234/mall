package com.cyh.mallproduct.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 属性值新增/修改 DTO
 *
 * 用于运营管理员和超级管理员对属性值进行增改操作
 */
@Data
public class AttributeValueDto {

    /**
     * 所属属性ID（新增时必填）
     */
    @NotNull(message = "属性ID不能为空")
    private Long attrId;

    /**
     * 属性值名称（如：黑色、8G、5000mAh等）
     */
    @NotBlank(message = "属性值不能为空")
    private String value;

    /**
     * 销售属性可配图片（如颜色色块）
     */
    private String imageUrl;

    /**
     * 排序（越小越靠前）
     */
    private Integer sort;
}