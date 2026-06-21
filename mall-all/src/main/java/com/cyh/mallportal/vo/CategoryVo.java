package com.cyh.mallportal.vo;

import lombok.Data;

/**
 * 分类 VO（公开/商户端）
 *
 * 用于前端展示分类基本信息，排除 isDeleted、createdAt、updatedAt 等内部字段
 * 对应接口：
 *   GET /category/detail/{id}
 *   GET /category/list
 *   GET /category/page
 *   GET /category/level1
 *   GET /category/children/{parentId}
 */
@Data
public class CategoryVo {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID（0 表示一级分类）
     */
    private Long parentId;

    /**
     * 分类级别（1-一级分类，2-二级分类，3-三级分类）
     */
    private Integer level;

    /**
     * 分类图标URL
     */
    private String icon;

    /**
     * 排序号（越小越靠前）
     */
    private Integer sort;

    /**
     * 状态（1-启用，0-禁用）
     */
    private Integer status;
}