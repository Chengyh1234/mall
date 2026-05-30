package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.StoreAdmin;

import java.util.List;

/**
 * 店铺管理员服务接口
 */
public interface StoreAdminService {

    /**
     * 新增店铺管理员
     * @param storeAdmin 管理员信息
     * @return 新增的管理员ID，失败返回null
     */
    Long add(StoreAdmin storeAdmin);

    /**
     * 更新店铺管理员
     * @param storeAdmin 管理员信息（需包含ID）
     * @return 更新成功返回true，失败返回false
     */
    boolean update(StoreAdmin storeAdmin);

    /**
     * 删除店铺管理员
     * @param id 管理员ID
     * @return 删除成功返回true
     */
    boolean delete(Long id);

    /**
     * 根据ID获取管理员详情
     * @param id 管理员ID
     * @return 管理员信息
     */
    StoreAdmin getById(Long id);

    /**
     * 根据店铺ID获取管理员列表
     * @param storeId 店铺ID
     * @return 管理员列表
     */
    List<StoreAdmin> getByStoreId(Long storeId);

    /**
     * 根据用户ID获取管理员记录
     * @param userId 用户ID
     * @return 管理员记录
     */
    StoreAdmin getByUserId(Long userId);

    /**
     * 检查用户是否为店铺管理员
     * @param storeId 店铺ID
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean isStoreAdmin(Long storeId, Long userId);

    /**
     * 批量添加店铺管理员
     * @param storeAdmins 管理员列表
     * @return 成功数量
     */
    int batchAdd(List<StoreAdmin> storeAdmins);

    /**
     * 更新管理员状态
     * @param id 管理员ID
     * @param status 状态
     * @return 更新成功返回true
     */
    boolean updateStatus(Long id, Integer status);
}