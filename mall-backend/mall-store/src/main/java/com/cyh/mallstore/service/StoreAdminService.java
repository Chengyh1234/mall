package com.cyh.mallstore.service;

import com.cyh.mallstore.entity.StoreAdmin;

import java.util.List;

/**
 * 店铺管理员服务接口
 * 定义店铺管理员的增删改查、权限校验、批量添加等功能
 */
public interface StoreAdminService {

    Long add(StoreAdmin storeAdmin);

    boolean update(StoreAdmin storeAdmin);

    boolean delete(Long id);

    StoreAdmin getById(Long id);

    List<StoreAdmin> getByStoreId(Long storeId);

    StoreAdmin getByUserId(Long userId);

    boolean isStoreAdmin(Long storeId, Long userId);

    int batchAdd(List<StoreAdmin> storeAdmins);

    boolean updateStatus(Long id, Integer status);
}