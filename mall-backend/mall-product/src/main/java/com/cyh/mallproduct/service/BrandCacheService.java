package com.cyh.mallproduct.service;

import com.cyh.mallproduct.entity.Brand;

import java.util.List;

/**
 * 品牌缓存服务接口
 * <p>
 * 缓存设计说明：
 * - brand:id:{id} → Brand 实体，TTL 30 分钟（Cache-Aside）
 * - brand:sort    → 全量排序品牌列表，TTL 30 分钟
 * - 写操作通过 afterCommit → MQ 异步清除精确 Key
 */
public interface BrandCacheService {

    /** 获取品牌 ID 缓存 */
    Brand getBrandById(Long id);

    /** 写入品牌 ID 缓存 */
    void setBrandById(Brand brand);

    /** 获取排序品牌列表缓存 */
    List<Brand> getBrandsBySort();

    /** 写入排序品牌列表缓存 */
    void setBrandsBySort(List<Brand> brands);

    /** 清除指定品牌的缓存（brand:id:{id}） */
    void clearBrandIdCache(Long id);
}