package com.cyh.mallstore.service.impl;

import com.cyh.mallstore.entity.StoreAdmin;
import com.cyh.mallstore.mapper.StoreAdminMapper;
import com.cyh.mallstore.service.StoreAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 店铺管理员服务实现类
 * 提供管理员账号的 CRUD 操作，按店铺 ID/用户 ID 查询等
 */
@Service
@RequiredArgsConstructor
public class StoreAdminServiceImpl implements StoreAdminService {

    private final StoreAdminMapper storeAdminMapper;

    /**
     * 新增管理员，默认状态为启用（1），默认角色为员工（2）
     */
    @Override
    public Long add(StoreAdmin storeAdmin) {
        storeAdmin.setCreatedAt(LocalDateTime.now());
        if (storeAdmin.getStatus() == null) {
            storeAdmin.setStatus(1);
        }
        if (storeAdmin.getRole() == null) {
            storeAdmin.setRole(2);
        }
        int result = storeAdminMapper.insert(storeAdmin);
        return result > 0 ? storeAdmin.getId() : null;
    }

    /**
     * 更新管理员信息
     */
    @Override
    public boolean update(StoreAdmin storeAdmin) {
        return storeAdminMapper.updateById(storeAdmin) > 0;
    }

    /**
     * 删除管理员
     */
    @Override
    public boolean delete(Long id) {
        return storeAdminMapper.deleteById(id) > 0;
    }

    /**
     * 按 ID 查询管理员详情
     */
    @Override
    public StoreAdmin getById(Long id) {
        return storeAdminMapper.selectById(id);
    }

    /**
     * 查询指定店铺的所有管理员列表
     */
    @Override
    public List<StoreAdmin> getByStoreId(Long storeId) {
        return storeAdminMapper.selectByStoreId(storeId);
    }

    /**
     * 按用户 ID 查询其管理员信息（用于判断用户是否是某个店铺的管理员）
     */
    @Override
    public StoreAdmin getByUserId(Long userId) {
        return storeAdminMapper.selectByUserId(userId);
    }

    /**
     * 校验指定用户是否为指定店铺的启用状态管理员
     */
    @Override
    public boolean isStoreAdmin(Long storeId, Long userId) {
        StoreAdmin admin = storeAdminMapper.selectByStoreIdAndUserId(storeId, userId);
        return admin != null && admin.getStatus() != null && admin.getStatus() == 1;
    }

    /**
     * 批量添加管理员，逐条插入并计数
     */
    @Override
    public int batchAdd(List<StoreAdmin> storeAdmins) {
        if (storeAdmins == null || storeAdmins.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (StoreAdmin storeAdmin : storeAdmins) {
            storeAdmin.setCreatedAt(LocalDateTime.now());
            if (storeAdmin.getStatus() == null) {
                storeAdmin.setStatus(1);
            }
            if (storeAdmin.getRole() == null) {
                storeAdmin.setRole(2);
            }
            count += storeAdminMapper.insert(storeAdmin) > 0 ? 1 : 0;
        }
        return count;
    }

    /**
     * 启用/禁用管理员（status=1 启用, status=0 禁用）
     */
    @Override
    public boolean updateStatus(Long id, Integer status) {
        StoreAdmin admin = new StoreAdmin();
        admin.setId(id);
        admin.setStatus(status);
        return storeAdminMapper.updateById(admin) > 0;
    }
}