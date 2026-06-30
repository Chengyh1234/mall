package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallportal.entity.Brand;
import com.cyh.mallportal.mapper.BrandMapper;
import com.cyh.mallportal.mq.event.CacheDomain;
import com.cyh.mallportal.mq.event.CacheInvalidateEvent;
import com.cyh.mallportal.mq.publisher.CacheEventPublisher;
import com.cyh.mallportal.service.BrandCacheService;
import com.cyh.mallportal.service.BrandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 品牌服务实现类
 * <p>
 * 缓存策略（Cache-Aside + afterCommit → MQ）：
 * - brand:id:{id} → 单条品牌详情（读多写少）
 * - brand:sort    → 全量排序品牌列表（前端选择器频繁调用）
 * - 品牌分页/条件列表组合参数多、命中率低，不缓存（参考 SPU 分页经验）
 */
@Slf4j
@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private BrandCacheService brandCacheService;

    @Autowired
    private CacheEventPublisher cacheEventPublisher;

    /**
     * 新增品牌
     * 事务提交后异步清除 brand:sort 缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(Brand brand) {
        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());
        if (brand.getStatus() == null) {
            brand.setStatus(1);
        }
        if (brand.getSort() == null) {
            brand.setSort(0);
        }
        int result = brandMapper.insert(brand);
        if (result > 0) {
            Long brandId = brand.getId();
            // 事务提交后，异步清除 brand:sort 缓存
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                            .setDomain(CacheDomain.BRAND)
                            .setExactKeys(List.of(RedisConstants.BRAND_SORT_KEY)));
                    log.info("新增品牌成功，品牌ID: {}, 已发布 brand:sort 缓存失效事件", brandId);
                }
            });
            return brandId;
        }
        return null;
    }

    /**
     * 根据ID删除品牌（逻辑删除）
     * 事务提交后异步清除 brand:id:{id} 和 brand:sort 缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        int result = brandMapper.deleteById(id);
        if (result > 0) {
            // 事务提交后，异步清除缓存
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    List<String> keys = new ArrayList<>();
                    keys.add(RedisConstants.BRAND_ID_PREFIX + id);
                    keys.add(RedisConstants.BRAND_SORT_KEY);
                    cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                            .setDomain(CacheDomain.BRAND)
                            .setExactKeys(keys));
                    log.info("删除品牌成功，品牌ID: {}, 已发布缓存失效事件", id);
                }
            });
            return true;
        }
        return false;
    }

    /**
     * 更新品牌信息
     * 事务提交后异步清除 brand:id:{id} 和 brand:sort 缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Brand brand) {
        brand.setUpdatedAt(LocalDateTime.now());
        int result = brandMapper.updateById(brand);
        if (result > 0) {
            Long brandId = brand.getId();
            // 事务提交后，异步清除缓存
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    List<String> keys = new ArrayList<>();
                    keys.add(RedisConstants.BRAND_ID_PREFIX + brandId);
                    keys.add(RedisConstants.BRAND_SORT_KEY);
                    cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                            .setDomain(CacheDomain.BRAND)
                            .setExactKeys(keys));
                    log.info("更新品牌成功，品牌ID: {}, 已发布缓存失效事件", brandId);
                }
            });
            return true;
        }
        return false;
    }

    /**
     * 根据ID获取品牌详情（Cache-Aside 模式）
     * 缓存 Key: brand:id:{id}，TTL: 30 分钟
     */
    @Override
    public Brand getById(Long id) {
        // Step1: 查缓存
        Brand cached = brandCacheService.getBrandById(id);
        if (cached != null) {
            log.debug("从缓存获取品牌成功，品牌ID: {}", id);
            return cached;
        }

        // Step2: 缓存未命中，查库
        Brand brand = brandMapper.selectById(id);

        // Step3: 回写缓存
        if (brand != null) {
            brandCacheService.setBrandById(brand);
            log.debug("从数据库查询品牌并缓存，品牌ID: {}", id);
        }
        return brand;
    }

    /**
     * 获取品牌列表（不分页）
     * 条件组合多、命中率低，不缓存，直接查库
     */
    @Override
    public List<Brand> getList(Brand brand) {
        LambdaQueryWrapper<Brand> queryWrapper = buildQueryWrapper(brand);
        queryWrapper.orderByAsc(Brand::getSort)
                .orderByDesc(Brand::getCreatedAt);
        return brandMapper.selectList(queryWrapper);
    }

    /**
     * 分页查询品牌
     * 条件组合多、命中率低，不缓存，直接查库
     */
    @Override
    public IPage<Brand> getPage(Brand brand, Integer page, Integer pageSize) {
        int pageNum = page != null ? page : 1;
        int pageSizeNum = pageSize != null ? pageSize : 10;

        Page<Brand> pageParam = new Page<>(pageNum, pageSizeNum);
        LambdaQueryWrapper<Brand> queryWrapper = buildQueryWrapper(brand);
        queryWrapper.orderByAsc(Brand::getSort)
                .orderByDesc(Brand::getCreatedAt);
        return brandMapper.selectPage(pageParam, queryWrapper);
    }

    /**
     * 根据状态查询品牌列表
     * 直接查库，不缓存
     */
    @Override
    public List<Brand> getByStatus(Integer status) {
        return brandMapper.selectByStatus(status);
    }

    /**
     * 根据品牌名称模糊查询品牌列表
     * 直接查库，不缓存
     */
    @Override
    public List<Brand> getByNameLike(String name) {
        return brandMapper.selectByNameLike(name);
    }

    /**
     * 按排序号查询品牌列表（Cache-Aside 模式）
     * 缓存 Key: brand:sort，TTL: 30 分钟
     * 仅用于启用状态的品牌，前端品牌选择器频繁调用
     */
    @Override
    public List<Brand> getBySort() {
        // Step1: 查缓存
        List<Brand> cached = brandCacheService.getBrandsBySort();
        if (cached != null) {
            log.debug("从缓存获取排序品牌列表成功");
            return cached;
        }

        // Step2: 缓存未命中，查库
        List<Brand> list = brandMapper.selectBySort();

        // Step3: 回写缓存
        if (list != null && !list.isEmpty()) {
            brandCacheService.setBrandsBySort(list);
            log.debug("从数据库查询排序品牌列表并缓存");
        }
        return list;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Brand> buildQueryWrapper(Brand brand) {
        LambdaQueryWrapper<Brand> queryWrapper = new LambdaQueryWrapper<>();
        if (brand == null) {
            return queryWrapper;
        }
        if (brand.getId() != null) {
            queryWrapper.eq(Brand::getId, brand.getId());
        }
        if (StringUtils.hasText(brand.getName())) {
            queryWrapper.like(Brand::getName, brand.getName());
        }
        if (brand.getStatus() != null) {
            queryWrapper.eq(Brand::getStatus, brand.getStatus());
        }
        return queryWrapper;
    }
}