package com.cyh.mallportal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分类-属性绑定排序修改 DTO
 *
 * 用于运营管理员和超级管理员调整分类下属性的排序
 */
@Data
public class CategoryAttributeSortDto {

    /**
     * 排序（越小越靠前）
     */
    @NotNull(message = "排序不能为空")
    private Integer sort;
}