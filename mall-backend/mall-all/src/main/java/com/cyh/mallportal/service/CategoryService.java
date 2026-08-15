package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.Category;
import com.cyh.mallportal.vo.CategoryTreeVo;

import java.util.List;

/**
 * 分类Service接口
 * 提供分类业务逻辑操作
 */
public interface CategoryService {

    /**
     * 新增分类
     *
     * @param category 分类实体
     * @return 分类ID
     */
    Long add(Category category);

    /**
     * 删除分类（级联删除子分类）
     *
     * @param id 分类ID
     * @return 是否删除成功
     */
    boolean delete(Long id);

    /**
     * 更新分类信息
     *
     * @param category 分类实体
     * @return 是否更新成功
     */
    boolean update(Category category);

    /**
     * 根据ID获取分类详情
     *
     * @param id 分类ID
     * @return 分类实体
     */
    Category getById(Long id);

    /**
     * 根据父分类ID查询子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> getByParentId(Long parentId);

    /**
     * 获取全部分类树形结构（从根节点构建整棵树，结果缓存到 Redis）
     *
     * @return 树形结构列表
     */
    List<CategoryTreeVo> getTreeWithChildren();

    /**
     * 获取指定分类ID及其所有子分类的ID列表
     * 用于查询分类及其子分类关联的数据
     *
     * @param categoryId 分类ID
     * @return 分类ID列表（包含传入的ID和所有子分类ID）
     */
    List<Long> getCategoryIdWithChildren(Long categoryId);
}