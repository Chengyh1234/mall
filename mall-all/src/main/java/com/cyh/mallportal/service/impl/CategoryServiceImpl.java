package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallportal.entity.Category;
import com.cyh.mallportal.entity.Spu;
import com.cyh.mallportal.mapper.CategoryMapper;
import com.cyh.mallportal.mapper.SpuMapper;
import com.cyh.mallportal.service.CategoryCacheService;
import com.cyh.mallportal.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
        
        // 清除所有分类缓存
        categoryCacheService.clearAllCategoryCache();
        log.info("新增分类成功, ID: {}, 已清除分类缓存", category.getId());
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

        // 清除所有分类缓存
        categoryCacheService.clearAllCategoryCache();
        
        log.info("删除分类成功（软删除）: {}, 关联删除商品: {}, 已清除分类缓存", id, spuCount);
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
            // 清除所有分类缓存
            categoryCacheService.clearAllCategoryCache();
            log.info("更新分类成功: {}, 已清除分类缓存", category.getId());
        } else {
            log.warn("更新分类失败: {}", category.getId());
        }

        return success;
    }

    @Override
    public Category getById(Long id) {
        // 尝试从缓存获取
        Category cached = categoryCacheService.getCategoryById(id);
        if (cached != null) {
            log.debug("从缓存获取分类成功，分类ID: {}", id);
            return cached;
        }
        
        // 缓存不存在，从数据库查询
        Category category = categoryMapper.selectById(id);
        if (category != null) {
            categoryCacheService.setCategoryById(category);
            log.debug("从数据库查询分类并缓存，分类ID: {}", id);
        }
        return category;
    }

    @Override
    public List<Category> getList(Category category) {
        // 生成缓存键
        String cacheKey = categoryCacheService.generateListCacheKey(category);
        
        // 尝试从缓存获取
        List<Category> cached = categoryCacheService.getCategoryList(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取分类列表成功，缓存键: {}", cacheKey);
            return cached;
        }
        
        // 缓存不存在，从数据库查询
        LambdaQueryWrapper<Category> wrapper = buildWrapper(category);
        wrapper.orderByAsc(Category::getSort);
        List<Category> categories = categoryMapper.selectList(wrapper);
        
        // 将结果放入缓存（永久存在）
        categoryCacheService.setCategoryList(cacheKey, categories);
        log.debug("从数据库查询分类列表并缓存，缓存键: {}", cacheKey);
        
        return categories;
    }

    @Override
    public List<Category> getPage(Category category, Integer page, Integer pageSize) {
        int pageNum = page != null ? page : 1;
        int pageSizeNum = pageSize != null ? pageSize : 10;
        
        // 生成缓存键
        String cacheKey = categoryCacheService.generatePageCacheKey(category, pageNum, pageSizeNum);
        
        // 尝试从缓存获取
        List<Category> cached = categoryCacheService.getCategoryList(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取分类分页列表成功，缓存键: {}", cacheKey);
            return cached;
        }
        
        // 缓存不存在，从数据库查询
        IPage<Category> pageInfo = new Page<>(pageNum, pageSizeNum);
        LambdaQueryWrapper<Category> wrapper = buildWrapper(category);
        wrapper.orderByAsc(Category::getSort);
        categoryMapper.selectPage(pageInfo, wrapper);
        List<Category> categories = pageInfo.getRecords();
        
        // 将结果放入缓存（永久存在）
        categoryCacheService.setCategoryList(cacheKey, categories);
        log.debug("从数据库查询分类分页列表并缓存，缓存键: {}", cacheKey);
        
        return categories;
    }

    @Override
    public List<Category> getByParentId(Long parentId) {
        // 尝试从缓存获取
        List<Category> cached = categoryCacheService.getChildrenByParentId(parentId);
        if (cached != null) {
            log.debug("从缓存获取子分类列表成功，父分类ID: {}", parentId);
            return cached;
        }
        
        // 缓存不存在，从数据库查询
        List<Category> children = categoryMapper.selectByParentId(parentId);
        categoryCacheService.setChildrenByParentId(parentId, children);
        log.debug("从数据库查询子分类列表并缓存，父分类ID: {}", parentId);
        return children;
    }

    @Override
    public List<Category> getByLevel(Integer level) {
        // 尝试从缓存获取
        List<Category> cached = categoryCacheService.getByLevel(level);
        if (cached != null) {
            log.debug("从缓存获取级别分类列表成功，级别: {}", level);
            return cached;
        }
        
        // 缓存不存在，从数据库查询
        List<Category> categories = categoryMapper.selectByLevel(level);
        categoryCacheService.setByLevel(level, categories);
        log.debug("从数据库查询级别分类列表并缓存，级别: {}", level);
        return categories;
    }

    @Override
    public List<Category> getByStatus(Integer status) {
        // 尝试从缓存获取
        List<Category> cached = categoryCacheService.getByStatus(status);
        if (cached != null) {
            log.debug("从缓存获取状态分类列表成功，状态: {}", status);
            return cached;
        }
        
        // 缓存不存在，从数据库查询
        List<Category> categories = categoryMapper.selectByStatus(status);
        categoryCacheService.setByStatus(status, categories);
        log.debug("从数据库查询状态分类列表并缓存，状态: {}", status);
        return categories;
    }

    @Override
    public List<Category> getTree(Long parentId) {
        if (parentId == null) {
            parentId = 0L;
        }
        List<Category> categories = getByParentId(parentId);
        for (Category category : categories) {
            List<Category> children = getTree(category.getId());
            if (!children.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
            }
        }
        return categories;
    }

    @Override
    public List<Map<String, Object>> getTreeWithChildren(Long parentId) {
        // 尝试从缓存获取
        List<Map<String, Object>> cached = categoryCacheService.getTreeWithChildren(parentId);
        if (cached != null) {
            log.debug("从缓存获取分类树成功，父分类ID: {}", parentId);
            return cached;
        }
        
        // 缓存不存在，从数据库查询并构建树
        if (parentId == null) {
            parentId = 0L;
        }
        List<Category> categories = getByParentId(parentId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Category category : categories) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", category.getId());
            map.put("name", category.getName());
            map.put("parentId", category.getParentId());
            map.put("level", category.getLevel());
            map.put("icon", category.getIcon());
            map.put("sort", category.getSort());
            map.put("status", category.getStatus());

            List<Map<String, Object>> children = getTreeWithChildren(category.getId());
            if (!children.isEmpty()) {
                map.put("children", children);
            }
            result.add(map);
        }
        
        // 将结果放入缓存
        categoryCacheService.setTreeWithChildren(parentId, result);
        log.debug("从数据库查询分类树并缓存，父分类ID: {}", parentId);
        
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
     * @param parentId   父分类ID
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

    /**
     * 构建查询条件
     *
     * @param category 查询条件实体
     * @return LambdaQueryWrapper
     */
    private LambdaQueryWrapper<Category> buildWrapper(Category category) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();

        if (category.getId() != null) {
            wrapper.eq(Category::getId, category.getId());
        }
        if (category.getName() != null && !category.getName().isEmpty()) {
            wrapper.like(Category::getName, category.getName());
        }
        if (category.getParentId() != null) {
            wrapper.eq(Category::getParentId, category.getParentId());
        }
        if (category.getLevel() != null) {
            wrapper.eq(Category::getLevel, category.getLevel());
        }
        if (category.getStatus() != null) {
            wrapper.eq(Category::getStatus, category.getStatus());
        }

        return wrapper;
    }
}
