package com.cyh.mallproduct.service;

import com.cyh.mallproduct.vo.CategoryTreeVo;

import java.util.List;

/**
 * 分类缓存服务接口
 * <p>
 * 只缓存根节点分类树（category:tree），访问 /category/tree 时从根节点构建整棵树并缓存。
 * 分类增删改时清除缓存，下次访问自动回源重建。
 */
public interface CategoryCacheService {

    /**
     * 获取缓存的根节点分类树
     *
     * @return 分类树结构列表，如果缓存不存在返回null
     */
    List<CategoryTreeVo> getTreeWithChildren();

    /**
     * 设置根节点分类树缓存
     *
     * @param tree 分类树结构
     */
    void setTreeWithChildren(List<CategoryTreeVo> tree);

    /**
     * 清除所有分类缓存
     */
    void clearAllCategoryCache();
}