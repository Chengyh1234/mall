package com.cyh.mallportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 属性新增/修改 DTO
 *
 * 用于运营管理员和超级管理员对属性进行增改操作
 */
@Data
public class AttributeDto {

    /**
     * 属性名称（如：颜色、内存、屏幕尺寸等）
     */
    @NotBlank(message = "属性名称不能为空")
    private String name;

    /**
     * 属性类型：1=销售属性，2=基本属性
     */
    @NotNull(message = "属性类型不能为空")
    private Integer attrType;

    /**
     * 排序（越小越靠前）
     */
    private Integer sort;
}