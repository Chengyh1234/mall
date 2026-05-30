package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallportal.entity.Store;
import com.cyh.mallportal.entity.StoreAdmin;
import com.cyh.mallportal.mapper.StoreAdminMapper;
import com.cyh.mallportal.mapper.StoreMapper;
import com.cyh.mallportal.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 店铺服务实现类
 * 提供店铺创建、修改、查询、权限管理等功能
 */
@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private StoreAdminMapper storeAdminMapper;

    /**
     * 添加新店铺
     * 自动设置创建时间、更新时间，默认状态为正常(1)，排序值为0
     *
     * @param store 店铺信息实体
     * @return 新增店铺的ID，失败返回null
     */
    @Override
    public Long add(Store store) {
        store.setCreatedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());
        if (store.getStatus() == null) {
            store.setStatus(1);
        }
        if (store.getSort() == null) {
            store.setSort(0);
        }
        int result = storeMapper.insert(store);
        return result > 0 ? store.getId() : null;
    }

    /**
     * 更新店铺信息
     * 自动更新修改时间
     *
     * @param store 待更新的店铺信息
     * @return 更新成功返回true，失败返回false
     */
    @Override
    public boolean update(Store store) {
        store.setUpdatedAt(LocalDateTime.now());
        return storeMapper.updateById(store) > 0;
    }

    /**
     * 根据ID查询店铺
     *
     * @param id 店铺ID
     * @return 店铺实体，不存在返回null
     */
    @Override
    public Store getById(Long id) {
        return storeMapper.selectById(id);
    }

    /**
     * 根据商家用户ID查询店铺
     *
     * @param sellerId 商家用户ID
     * @return 店铺实体，不存在返回null
     */
    @Override
    public Store getBySellerId(Long sellerId) {
        return storeMapper.selectBySellerId(sellerId);
    }

    /**
     * 根据条件查询店铺列表
     * 结果按创建时间倒序排列
     *
     * @param store 查询条件实体
     * @return 店铺列表
     */
    @Override
    public List<Store> getList(Store store) {
        LambdaQueryWrapper<Store> queryWrapper = buildQueryWrapper(store);
        queryWrapper.orderByDesc(Store::getCreatedAt);
        return storeMapper.selectList(queryWrapper);
    }

    /**
     * 分页查询店铺列表
     * 支持关键字搜索和状态筛选
     *
     * @param keyword 关键字，模糊搜索店铺名称
     * @param status 店铺状态，可选
     * @param page 页码，从1开始
     * @param pageSize 每页条数，默认10
     * @return 店铺分页列表
     */
    @Override
    public List<Store> getPage(String keyword, Integer status, Integer page, Integer pageSize) {
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null ? pageSize : 10);
        int limit = pageSize != null ? pageSize : 10;
        return storeMapper.selectPage(keyword, status, offset, limit);
    }

    /**
     * 统计分页查询的总数
     *
     * @param keyword 关键字，模糊搜索店铺名称
     * @param status 店铺状态，可选
     * @return 店铺总数
     */
    @Override
    public int countPage(String keyword, Integer status) {
        return storeMapper.countPage(keyword, status);
    }

    /**
     * 更新店铺状态
     * 同时自动更新修改时间
     *
     * @param id 店铺ID
     * @param status 新状态
     * @return 更新成功返回true，失败返回false
     */
    @Override
    public boolean updateStatus(Long id, Integer status) {
        Store store = new Store();
        store.setId(id);
        store.setStatus(status);
        store.setUpdatedAt(LocalDateTime.now());
        return storeMapper.updateById(store) > 0;
    }

    /**
     * 判断用户是否为店铺所有者
     *
     * @param storeId 店铺ID
     * @param userId 用户ID
     * @return 是店铺所有者返回true，否则返回false
     */
    @Override
    public boolean isStoreOwner(Long storeId, Long userId) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            return false;
        }
        return store.getSellerId() != null && store.getSellerId().equals(userId);
    }

    /**
     * 判断用户是否为店铺管理员
     * 需同时满足：存在管理员记录且管理员状态正常(1)
     *
     * @param storeId 店铺ID
     * @param userId 用户ID
     * @return 是店铺管理员返回true，否则返回false
     */
    @Override
    public boolean isStoreAdmin(Long storeId, Long userId) {
        StoreAdmin admin = storeAdminMapper.selectByStoreIdAndUserId(storeId, userId);
        return admin != null && admin.getStatus() != null && admin.getStatus() == 1;
    }

    /**
     * 判断用户是否拥有店铺操作权限
     * 只要满足：是店铺所有者 OR 是店铺管理员
     *
     * @param storeId 店铺ID
     * @param userId 用户ID
     * @return 拥有权限返回true，否则返回false
     */
    @Override
    public boolean hasStorePermission(Long storeId, Long userId) {
        if (isStoreOwner(storeId, userId)) {
            return true;
        }
        if (isStoreAdmin(storeId, userId)) {
            return true;
        }
        return false;
    }

    /**
     * 构建店铺查询条件Wrapper
     * 支持ID、商家ID、状态、店铺名称模糊搜索
     *
     * @param store 查询条件实体
     * @return MyBatis-Plus查询Wrapper
     */
    private LambdaQueryWrapper<Store> buildQueryWrapper(Store store) {
        LambdaQueryWrapper<Store> queryWrapper = new LambdaQueryWrapper<>();
        if (store == null) {
            return queryWrapper;
        }
        if (store.getId() != null) {
            queryWrapper.eq(Store::getId, store.getId());
        }
        if (store.getSellerId() != null) {
            queryWrapper.eq(Store::getSellerId, store.getSellerId());
        }
        if (store.getStatus() != null) {
            queryWrapper.eq(Store::getStatus, store.getStatus());
        }
        if (StringUtils.hasText(store.getName())) {
            queryWrapper.like(Store::getName, store.getName());
        }
        return queryWrapper;
    }
}
