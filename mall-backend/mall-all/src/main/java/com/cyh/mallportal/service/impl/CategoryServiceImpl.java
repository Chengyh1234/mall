package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallportal.entity.Category;
import com.cyh.mallportal.entity.Spu;
import com.cyh.mallportal.mapper.CategoryMapper;
import com.cyh.mallportal.mapper.SpuMapper;
import com.cyh.mallportal.mq.event.CacheDomain;
import com.cyh.mallportal.mq.event.CacheInvalidateEvent;
import com.cyh.mallportal.mq.publisher.CacheEventPublisher;
import com.cyh.mallportal.service.CategoryCacheService;
import com.cyh.mallportal.service.CategoryService;
import com.cyh.mallportal.vo.CategoryTreeVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类Service实现类
 * 提供分类业务逻辑的具体实现
 * 支持软删除及关联数据的级联软删除
 * 支持Redis缓存优化查询性能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final SpuMapper spuMapper;
    private final CategoryCacheService categoryCacheService;
    private final CacheEventPublisher cacheEventPublisher;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(Category category) {
        log.info("新增分类: {}", category.getName());

        if (category.getParentId() == null) {
            category.setParentId(0L);
        }

        if (category.getLevel() == null) {
            if (category.getParentId() == 0) {
                category.setLevel(1);
            } else {
                Category parent = categoryMapper.selectById(category.getParentId());
                category.setLevel(parent != null ? parent.getLevel() + 1 : 1);
            }
        }

        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }

        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        categoryMapper.insert(category);

        // 事务提交后，异步清除分类缓存
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                        .setDomain(CacheDomain.CATEGORY)
                        .setExactKeys(List.of(RedisConstants.CATEGORY_TREE_KEY)));
            }
        });
        log.info("新增分类成功, ID: {}, 已发布缓存失效事件", category.getId());
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        log.info("删除分类（软删除）: {}", id);

        // 1. 查询要删除的分类
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            log.warn("分类不存在: {}", id);
            return false;
        }

        // 2. 先逻辑删除该分类下的所有关联数据（必须在删除子分类之前）
        int spuCount = softDeleteSpuByCategoryId(id);


        // 3. 查询该分类的子分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, id);
        List<Category> children = categoryMapper.selectList(wrapper);

        // 4. 递归删除子分类（子分类的关联数据会在递归中先被删除）
        for (Category child : children) {
            delete(child.getId());
        }

        // 5. 逻辑删除当前分类
        // 调用deleteById让MyBatis-Plus自动转换为 UPDATE SET is_deleted=1
        categoryMapper.deleteById(id);

        // 事务提交后，异步清除分类缓存
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                        .setDomain(CacheDomain.CATEGORY)
                        .setExactKeys(List.of(RedisConstants.CATEGORY_TREE_KEY)));
            }
        });

        log.info("删除分类成功（软删除）: {}, 关联删除商品: {}, 已发布缓存失效事件", id, spuCount);
        return true;
    }

    /**
     * 软删除该分类下的所有商品（设置 is_deleted=1）
     *
     * @param categoryId 分类ID
     * @return 被逻辑删除的商品数量
     */
    private int softDeleteSpuByCategoryId(Long categoryId) {
        // 查询该分类下的所有商品（包含已下架的）
        List<Spu> spuList = spuMapper.selectAllByCategoryId(categoryId);

        int deletedCount = 0;
        for (Spu spu : spuList) {
            // 调用deleteById让MyBatis-Plus自动转换为 UPDATE SET is_deleted=1
            spuMapper.deleteById(spu.getId());
            deletedCount++;
            log.debug("逻辑删除商品SPU: {}", spu.getId());
        }

        if (!spuList.isEmpty()) {
            log.info("该分类下共有 {} 个商品被逻辑删除", deletedCount);
        }
        return deletedCount;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Category category) {
        log.info("更新分类: {}", category.getId());

        category.setUpdatedAt(LocalDateTime.now());
        int rows = categoryMapper.updateById(category);

        boolean success = rows > 0;
        if (success) {
            // 事务提交后，异步清除分类缓存
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                            .setDomain(CacheDomain.CATEGORY)
                            .setExactKeys(List.of(RedisConstants.CATEGORY_TREE_KEY)));
                }
            });
            log.info("更新分类成功: {}, 已发布缓存失效事件", category.getId());
        } else {
            log.warn("更新分类失败: {}", category.getId());
        }

        return success;
    }

    @Override
    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public List<Category> getByParentId(Long parentId) {
        return categoryMapper.selectByParentId(parentId);
    }

    @Override
    public List<CategoryTreeVo> getTreeWithChildren() {
        // 尝试从缓存获取
        List<CategoryTreeVo> cached = categoryCacheService.getTreeWithChildren();
        if (cached != null) {
            log.debug("从缓存获取分类树成功");
            return cached;
        }

        // 缓存不存在，从根节点递归构建树
        List<CategoryTreeVo> result = buildTreeRecursive(0L);

        // 将结果放入缓存
        categoryCacheService.setTreeWithChildren(result);
        log.debug("从数据库查询分类树并缓存");

        return result;
    }

    /**
     * 递归构建分类树
     *
     * @param parentId 父分类ID
     * @return 子节点树形结构列表
     */
    private List<CategoryTreeVo> buildTreeRecursive(Long parentId) {
        List<Category> categories = getByParentId(parentId);
        List<CategoryTreeVo> result = new ArrayList<>();

        for (Category category : categories) {
            CategoryTreeVo vo = new CategoryTreeVo();
            vo.setId(category.getId());
            vo.setName(category.getName());
            vo.setParentId(category.getParentId());
            vo.setIcon(category.getIcon());
            vo.setSort(category.getSort());

            List<CategoryTreeVo> children = buildTreeRecursive(category.getId());
            if (!children.isEmpty()) {
                vo.setChildren(children);
            }
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<Long> getCategoryIdWithChildren(Long categoryId) {
        List<Long> categoryIds = new ArrayList<>();
        if (categoryId == null) {
            return categoryIds;
        }

        // 先添加传入的分类ID
        categoryIds.add(categoryId);

        // 递归获取所有子分类ID
        collectChildCategoryIds(categoryId, categoryIds);

        return categoryIds;
    }

    /**
     * 递归收集子分类ID
     *
     * @param parentId     父分类ID
     * @param categoryIds 分类ID列表（会被修改）
     */
    private void collectChildCategoryIds(Long parentId, List<Long> categoryIds) {
        // 查询所有子分类
        List<Category> children = getByParentId(parentId);
        for (Category child : children) {
            categoryIds.add(child.getId());
            // 递归收集子分类的子分类
            collectChildCategoryIds(child.getId(), categoryIds);
        }
    }
}