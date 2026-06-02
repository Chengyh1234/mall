package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallportal.entity.Brand;
import com.cyh.mallportal.mapper.BrandMapper;
import com.cyh.mallportal.service.BrandCacheService;
import com.cyh.mallportal.service.BrandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 品牌服务实现类
 * 实现品牌的新增、删除、修改、查询等功能
 * 支持Redis缓存优化查询性能
 */
@Slf4j
@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private BrandCacheService brandCacheService;

    /**
     * 新增品牌
     *
     * @param brand 品牌信息
     * @return 新增的品牌ID，失败返回null
     */
    @Override
    public Long add(Brand brand) {
        try {
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
                brandCacheService.clearAllBrandCache();
                log.info("新增品牌成功，品牌ID: {}, 品牌名称: {}, 已清除品牌缓存", brand.getId(), brand.getName());
                return brand.getId();
            }
        } catch (Exception e) {
            log.error("新增品牌失败，品牌名称: {}", brand.getName(), e);
        }
        return null;
    }

    /**
     * 根据ID删除品牌（逻辑删除）
     *
     * @param id 品牌ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    public boolean delete(Long id) {
        try {
            int result = brandMapper.deleteById(id);
            if (result > 0) {
                brandCacheService.clearAllBrandCache();
                log.info("删除品牌成功，品牌ID: {}, 已清除品牌缓存", id);
                return true;
            }
        } catch (Exception e) {
            log.error("删除品牌失败，品牌ID: {}", id, e);
        }
        return false;
    }

    /**
     * 更新品牌信息
     *
     * @param brand 品牌信息（需包含ID）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    public boolean update(Brand brand) {
        try {
            brand.setUpdatedAt(LocalDateTime.now());
            int result = brandMapper.updateById(brand);
            if (result > 0) {
                brandCacheService.clearAllBrandCache();
                log.info("更新品牌成功，品牌ID: {}, 品牌名称: {}, 已清除品牌缓存", brand.getId(), brand.getName());
                return true;
            }
        } catch (Exception e) {
            log.error("更新品牌失败，品牌ID: {}", brand.getId(), e);
        }
        return false;
    }

    /**
     * 根据ID获取品牌详情
     *
     * @param id 品牌ID
     * @return 品牌信息，不存在返回null
     */
    @Override
    public Brand getById(Long id) {
        Brand cached = brandCacheService.getBrandById(id);
        if (cached != null) {
            log.debug("从缓存获取品牌成功，品牌ID: {}", id);
            return cached;
        }

        try {
            Brand brand = brandMapper.selectById(id);
            if (brand != null) {
                brandCacheService.setBrandById(brand);
                log.debug("从数据库查询品牌并缓存，品牌ID: {}", id);
            }
            return brand;
        } catch (Exception e) {
            log.error("查询品牌详情失败，品牌ID: {}", id, e);
            return null;
        }
    }

    /**
     * 获取品牌列表（不分页）
     *
     * @param brand 查询条件（支持id、name、status）
     * @return 品牌列表
     */
    @Override
    public List<Brand> getList(Brand brand) {
        String cacheKey = brandCacheService.generateListCacheKey(brand);

        List<Brand> cached = brandCacheService.getBrandList(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取品牌列表成功，缓存键: {}", cacheKey);
            return cached;
        }

        try {
            LambdaQueryWrapper<Brand> queryWrapper = buildQueryWrapper(brand);
            queryWrapper.orderByAsc(Brand::getSort)
                    .orderByDesc(Brand::getCreatedAt);
            List<Brand> list = brandMapper.selectList(queryWrapper);
            brandCacheService.setBrandList(cacheKey, list);
            log.debug("从数据库查询品牌列表并缓存，缓存键: {}", cacheKey);
            return list;
        } catch (Exception e) {
            log.error("查询品牌列表失败", e);
            return null;
        }
    }

    /**
     * 分页查询品牌
     *
     * @param brand 查询条件（支持id、name、status）
     * @param page 页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 品牌分页列表
     */
    @Override
    public IPage<Brand> getPage(Brand brand, Integer page, Integer pageSize) {
        int pageNum = page != null ? page : 1;
        int pageSizeNum = pageSize != null ? pageSize : 10;

        String cacheKey = brandCacheService.generatePageCacheKey(brand, pageNum, pageSizeNum);
        String countKey = brandCacheService.generatePageCountKey(brand);

        List<Brand> cachedList = brandCacheService.getBrandList(cacheKey);
        Integer cachedTotal = brandCacheService.getBrandCount(countKey);

        if (cachedList != null && cachedTotal != null) {
            log.debug("从缓存获取品牌分页列表成功，缓存键: {}", cacheKey);
            Page<Brand> pageResult = new Page<>(pageNum, pageSizeNum);
            pageResult.setRecords(cachedList);
            pageResult.setTotal(cachedTotal);
            return pageResult;
        }

        try {
            Page<Brand> pageParam = new Page<>(pageNum, pageSizeNum);
            LambdaQueryWrapper<Brand> queryWrapper = buildQueryWrapper(brand);
            queryWrapper.orderByAsc(Brand::getSort)
                    .orderByDesc(Brand::getCreatedAt);
            Page<Brand> pageResult = brandMapper.selectPage(pageParam, queryWrapper);

            brandCacheService.setBrandList(cacheKey, pageResult.getRecords());
            brandCacheService.setBrandCount(countKey, (int) pageResult.getTotal());
            log.debug("从数据库查询品牌分页列表并缓存，缓存键: {}", cacheKey);
            return pageResult;
        } catch (Exception e) {
            log.error("分页查询品牌失败", e);
            return null;
        }
    }

    /**
     * 根据状态查询品牌列表
     *
     * @param status 状态（1-启用 0-禁用）
     * @return 品牌列表，按排序号升序、创建时间降序排列
     */
    @Override
    public List<Brand> getByStatus(Integer status) {
        List<Brand> cached = brandCacheService.getBrandsByStatus(status);
        if (cached != null) {
            log.debug("从缓存获取状态品牌列表成功，状态: {}", status);
            return cached;
        }

        try {
            List<Brand> list = brandMapper.selectByStatus(status);
            brandCacheService.setBrandsByStatus(status, list);
            log.debug("从数据库查询状态品牌列表并缓存，状态: {}", status);
            return list;
        } catch (Exception e) {
            log.error("根据状态查询品牌列表失败，状态: {}", status, e);
            return null;
        }
    }

    /**
     * 根据品牌名称模糊查询品牌列表
     *
     * @param name 品牌名称（支持模糊匹配）
     * @return 品牌列表，只返回启用状态的品牌
     */
    @Override
    public List<Brand> getByNameLike(String name) {
        try {
            return brandMapper.selectByNameLike(name);
        } catch (Exception e) {
            log.error("根据品牌名称模糊查询失败，品牌名称: {}", name, e);
            return null;
        }
    }

    /**
     * 按排序号查询品牌列表
     *
     * @return 品牌列表，只返回启用状态的品牌，按排序号升序排列
     */
    @Override
    public List<Brand> getBySort() {
        List<Brand> cached = brandCacheService.getBrandsBySort();
        if (cached != null) {
            log.debug("从缓存获取排序品牌列表成功");
            return cached;
        }

        try {
            List<Brand> list = brandMapper.selectBySort();
            brandCacheService.setBrandsBySort(list);
            log.debug("从数据库查询排序品牌列表并缓存");
            return list;
        } catch (Exception e) {
            log.error("按排序号查询品牌列表失败", e);
            return null;
        }
    }

    /**
     * 构建查询条件
     *
     * @param brand 查询条件对象
     * @return LambdaQueryWrapper查询构造器
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