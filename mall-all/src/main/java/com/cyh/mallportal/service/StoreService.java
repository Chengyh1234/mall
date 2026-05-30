package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.Store;

import java.util.List;

/**
 * 店铺服务接口
 */
public interface StoreService {

    /**
     * 新增店铺
     * @param store 店铺信息
     * @return 新增的店铺ID，失败返回null
     */
    Long add(Store store);

    /**
     * 更新店铺信息
     * @param store 店铺信息（需包含ID）
     * @return 更新成功返回true，失败返回false
     */
    boolean update(Store store);

    /**
     * 根据ID获取店铺详情
     * @param id 店铺ID
     * @return 店铺信息，不存在返回null
     */
    Store getById(Long id);

    /**
     * 根据商家ID获取店铺
     * @param sellerId 商家用户ID
     * @return 店铺信息
     */
    Store getBySellerId(Long sellerId);

    /**
     * 获取店铺列表
     * @param store 查询条件
     * @return 店铺列表
     */
    List<Store> getList(Store store);

    /**
     * 分页获取店铺列表
     * @param keyword 搜索关键字
     * @param status 状态筛选
     * @param page 页码
     * @param pageSize 每页数量
     * @return 店铺列表
     */
    List<Store> getPage(String keyword, Integer status, Integer page, Integer pageSize);

    /**
     * 统计分页数量
     * @param keyword 搜索关键字
     * @param status 状态筛选
     * @return 总数量
     */
    int countPage(String keyword, Integer status);

    /**
     * 更新店铺状态
     * @param id 店铺ID
     * @param status 状态
     * @return 更新成功返回true
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 检查用户是否为店铺店长
     * @param storeId 店铺ID
     * @param userId 用户ID
     * @return 是否为店长
     */
    boolean isStoreOwner(Long storeId, Long userId);

    /**
     * 检查用户是否为店铺管理员
     * @param storeId 店铺ID
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean isStoreAdmin(Long storeId, Long userId);

    /**
     * 检查用户是否有店铺操作权限
     * @param storeId 店铺ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasStorePermission(Long storeId, Long userId);
}