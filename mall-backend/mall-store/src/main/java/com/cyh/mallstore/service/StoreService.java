package com.cyh.mallstore.service;

import com.cyh.mallstore.entity.Store;
import com.cyh.mallstore.vo.StoreDetailVo;
import com.cyh.mallstore.vo.StoreVo;

import java.util.List;

/**
 * 店铺服务接口
 * 定义店铺的 CRUD、开店申请流程、审核、缓存管理等功能
 */
public interface StoreService {

    Long add(Store store);

    boolean update(Store store);

    Store getById(Long id);

    StoreDetailVo getDetailVO(Long id);

    Store getBySellerId(Long sellerId);

    List<Store> getList(Store store);

    List<Store> getPage(String keyword, Integer status, Integer page, Integer pageSize);

    int countPage(String keyword, Integer status);

    List<StoreVo> getPageVO(String keyword, Integer page, Integer pageSize);

    int countPageVO(String keyword);

    List<Store> getAdminPage(Long id, String keyword, Integer status,
                             Long sellerId, String phone,
                             Integer page, Integer pageSize);

    int countAdminPage(Long id, String keyword, Integer status,
                       Long sellerId, String phone);

    boolean updateStatus(Long id, Integer status);

    boolean isStoreOwner(Long storeId, Long userId);

    boolean isStoreAdmin(Long storeId, Long userId);

    boolean hasStorePermission(Long storeId, Long userId);

    // ========== 开店申请 ==========

    Store apply(Long userId, String name, String description, String phone, String address);

    void approve(Long id);

    void reject(Long id, String rejectReason);

    void reSubmit(Long storeId, String name, String description, String phone, String address);

    void reApply(Long storeId, Long userId, String name, String description, String phone, String address);

    Store getMyApply(Long userId);

    List<Store> getPendingPage(Integer status, Integer page, Integer pageSize);

    int countPending(Integer status);

    void deactivate(Long storeId, Long userId);
}