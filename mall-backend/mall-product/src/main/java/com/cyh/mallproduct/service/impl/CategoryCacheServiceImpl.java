package com.cyh.mallproduct.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.utils.RedisUtils;
import com.cyh.mallproduct.service.CategoryCacheService;
import com.cyh.mallproduct.vo.CategoryTreeVo;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类缓存服务实现类
 * <p>
 * 只缓存根节点分类树（category:tree），访问 /category/tree 时从根节点构建整棵树并缓存。
 * 分类增删改时清除缓存，下次访问自动回源重建。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryCacheServiceImpl implements CategoryCacheService {

    private final RedisUtils redisUtils;

    @Override
    public List<CategoryTreeVo> getTreeWithChildren() {
        String json = redisUtils.get(RedisConstants.CATEGORY_TREE_KEY);
        if (json != null) {
            log.debug("从缓存获取分类树成功");
            return redisUtils.fromJson(json, new TypeReference<List<CategoryTreeVo>>() {});
        }
        return null;
    }

    @Override
    public void setTreeWithChildren(List<CategoryTreeVo> tree) {
        redisUtils.setObject(RedisConstants.CATEGORY_TREE_KEY, tree);
        log.debug("设置分类树缓存成功（永久）");
    }

    @Override
    public void clearAllCategoryCache() {
        boolean deleted = redisUtils.delete(RedisConstants.CATEGORY_TREE_KEY);
        if (deleted) {
            log.info("清除分类树缓存成功");
        }
    }
}