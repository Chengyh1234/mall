package com.cyh.mallstore.service;

import com.cyh.mallstore.entity.Banner;

import java.util.List;

/**
 * 轮播图服务接口
 * 定义轮播图的 CRUD、状态管理、活跃列表查询等功能
 */
public interface BannerService {

    Long add(Banner banner);

    boolean update(Banner banner);

    boolean delete(Long id);

    boolean updateStatus(Long id, Integer status);

    Banner getById(Long id);

    List<Banner> getList(Integer status);

    List<Banner> getActiveList();
}