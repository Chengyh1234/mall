package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.Category;

import java.util.List;
import java.util.Map;

/**
 * 分类缓存服务接口
 */
public interface CategoryCacheService {

    /**
     * 获取缓存的单个分类
     *
     * @param id 分类ID
     * @return 分类实体，如果缓存不存在返回null
     */
    Category getCategoryById(Long id);

    /**
     * 设置单个分类缓存
     *
     * @param category 分类实体
     */
    void setCategoryById(Category category);

    /**
     * 获取缓存的子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表，如果缓存不存在返回null
     */
    List<Category> getChildrenByParentId(Long parentId);

    /**
     * 设置子分类列表缓存
     *
     * @param parentId 父分类ID
     * @param children 子分类列表
     */
    void setChildrenByParentId(Long parentId, List<Category> children);

    /**
     * 获取缓存的指定级别分类列表
     *
     * @param level 分类级别
     * @return 分类列表，如果缓存不存在返回null
     */
    List<Category> getByLevel(Integer level);

    /**
     * 设置指定级别分类列表缓存
     *
     * @param level      分类级别
     * @param categories 分类列表
     */
    void setByLevel(Integer level, List<Category> categories);

    /**
     * 获取缓存的指定状态分类列表
     *
     * @param status 状态
     * @return 分类列表，如果缓存不存在返回null
     */
    List<Category> getByStatus(Integer status);

    /**
     * 设置指定状态分类列表缓存
     *
     * @param status     状态
     * @param categories 分类列表
     */
    void setByStatus(Integer status, List<Category> categories);

    /**
     * 获取缓存的分类树结构
     *
     * @param parentId 父分类ID
     * @return 分类树结构列表，如果缓存不存在返回null
     */
    List<Map<String, Object>> getTreeWithChildren(Long parentId);

    /**
     * 设置分类树结构缓存
     *
     * @param parentId 父分类ID
     * @param tree     分类树结构
     */
    void setTreeWithChildren(Long parentId, List<Map<String, Object>> tree);

    /**
     * 清除所有分类缓存
     */
    void clearAllCategoryCache();

    /**
     * 清除指定分类ID的缓存
     *
     * @param id 分类ID
     */
    void clearCategoryCache(Long id);

    /**
     * 获取缓存的分类列表
     *
     * @param cacheKey 缓存键
     * @return 分类列表，如果缓存不存在返回null
     */
    List<Category> getCategoryList(String cacheKey);

    /**
     * 设置分类列表缓存
     *
     * @param cacheKey    缓存键
     * @param categories 分类列表
     */
    void setCategoryList(String cacheKey, List<Category> categories);

    /**
     * 生成分页查询缓存键
     *
     * @param category 查询条件
     * @param page     页码
     * @param pageSize 每页数量
     * @return 缓存键
     */
    String generatePageCacheKey(Category category, Integer page, Integer pageSize);

    /**
     * 生成列表查询缓存键
     *
     * @param category 查询条件
     * @return 缓存键
     */
    String generateListCacheKey(Category category);
}