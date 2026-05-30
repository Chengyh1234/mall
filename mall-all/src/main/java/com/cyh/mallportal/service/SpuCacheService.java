package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.Spu;

import java.util.List;

/**
 * SPU缓存服务接口
 */
public interface SpuCacheService {

    /**
     * 获取缓存的商品列表
     *
     * @param cacheKey 缓存键
     * @return 商品列表，如果缓存不存在返回null
     */
    List<Spu> getSpuList(String cacheKey);

    /**
     * 设置商品列表缓存
     *
     * @param cacheKey 缓存键
     * @param spuList  商品列表
     */
    void setSpuList(String cacheKey, List<Spu> spuList);

    /**
     * 获取缓存的商品总数
     *
     * @param countKey 总数缓存键
     * @return 商品总数，如果缓存不存在返回null
     */
    Integer getSpuCount(String countKey);

    /**
     * 设置商品总数缓存
     *
     * @param countKey 总数缓存键
     * @param count    商品总数
     */
    void setSpuCount(String countKey, int count);

    /**
     * 清除所有商品相关缓存
     */
    void clearAllSpuCache();

    /**
     * 清除指定缓存键的缓存
     *
     * @param cacheKey 缓存键
     */
    void clearCache(String cacheKey);

    /**
     * 生成分页查询缓存键
     *
     * @param categoryId 分类ID（可为null）
     * @param brandId    品牌ID（可为null）
     * @param keyword    搜索关键字（可为null）
     * @param status     状态（可为null）
     * @param page       页码
     * @param pageSize   每页数量
     * @return 缓存键
     */
    String generateCacheKey(Long categoryId, Long brandId, String keyword, Integer status, Integer page, Integer pageSize);

    /**
     * 生成总数缓存键
     *
     * @param categoryId 分类ID（可为null）
     * @param brandId    品牌ID（可为null）
     * @param keyword    搜索关键字（可为null）
     * @param status     状态（可为null）
     * @return 总数缓存键
     */
    String generateCountKey(Long categoryId, Long brandId, String keyword, Integer status);
}