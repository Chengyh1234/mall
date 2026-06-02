package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.Brand;

import java.util.List;

/**
 * 品牌缓存服务接口
 */
public interface BrandCacheService {

    Brand getBrandById(Long id);

    void setBrandById(Brand brand);

    List<Brand> getBrandsByStatus(Integer status);

    void setBrandsByStatus(Integer status, List<Brand> brands);

    List<Brand> getBrandsBySort();

    void setBrandsBySort(List<Brand> brands);

    List<Brand> getBrandList(String cacheKey);

    void setBrandList(String cacheKey, List<Brand> brands);

    void clearAllBrandCache();

    String generatePageCacheKey(Brand brand, Integer page, Integer pageSize);

    String generatePageCountKey(Brand brand);

    String generateListCacheKey(Brand brand);

    Integer getBrandCount(String countKey);

    void setBrandCount(String countKey, int count);
}