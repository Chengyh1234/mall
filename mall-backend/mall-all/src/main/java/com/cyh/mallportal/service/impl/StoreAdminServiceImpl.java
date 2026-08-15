package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallportal.entity.StoreAdmin;
import com.cyh.mallportal.mapper.StoreAdminMapper;
import com.cyh.mallportal.service.StoreAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 店铺管理员服务实现类
 */
@Service
public class StoreAdminServiceImpl implements StoreAdminService {

    @Autowired
    private StoreAdminMapper storeAdminMapper;

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

    @Override
    public boolean update(StoreAdmin storeAdmin) {
        return storeAdminMapper.updateById(storeAdmin) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return storeAdminMapper.deleteById(id) > 0;
    }

    @Override
    public StoreAdmin getById(Long id) {
        return storeAdminMapper.selectById(id);
    }

    @Override
    public List<StoreAdmin> getByStoreId(Long storeId) {
        return storeAdminMapper.selectByStoreId(storeId);
    }

    @Override
    public StoreAdmin getByUserId(Long userId) {
        return storeAdminMapper.selectByUserId(userId);
    }

    @Override
    public boolean isStoreAdmin(Long storeId, Long userId) {
        StoreAdmin admin = storeAdminMapper.selectByStoreIdAndUserId(storeId, userId);
        return admin != null && admin.getStatus() != null && admin.getStatus() == 1;
    }

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

    @Override
    public boolean updateStatus(Long id, Integer status) {
        StoreAdmin admin = new StoreAdmin();
        admin.setId(id);
        admin.setStatus(status);
        return storeAdminMapper.updateById(admin) > 0;
    }
}