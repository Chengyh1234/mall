package com.cyh.mallproduct.vo;

import lombok.Data;

import java.util.List;

/**
 * 分类树形结构 VO（公开/商户端）
 *
 * 用于展示分类的树形层级关系，替代原有手搓 Map 的方式
 * 对应接口：
 *   GET /category/tree
 */
@Data
public class CategoryTreeVo {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类图标URL
     */
    private String icon;

    /**
     * 排序（越小越靠前）
     */
    private Integer sort;

    /**
     * 子分类列表（递归结构）
     */
    private List<CategoryTreeVo> children;
}