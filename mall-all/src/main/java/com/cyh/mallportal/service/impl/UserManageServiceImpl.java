package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallportal.entity.Role;
import com.cyh.mallportal.entity.Store;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.entity.UserRole;
import com.cyh.mallportal.mapper.RoleMapper;
import com.cyh.mallportal.mapper.StoreMapper;
import com.cyh.mallportal.mapper.UserMapper;
import com.cyh.mallportal.mapper.UserRoleMapper;
import com.cyh.mallportal.service.UserManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理 Service 实现类（后台管理用）
 *
 * 提供运营管理员和超级管理员对用户的管理功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManageServiceImpl implements UserManageService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final StoreMapper storeMapper;

    /**
     * 商家角色的编码（从数据库 roles 表获取）
     */
    private static final String SELLER_ROLE_CODE = "SELLER";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> promoteToSeller(Long userId) {
        log.info("将用户升级为商家: userId={}", userId);

        // 1. 校验用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 查询商家角色
        Role sellerRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getCode, SELLER_ROLE_CODE)
        );
        if (sellerRole == null) {
            throw new BusinessException("商家角色不存在，请联系超级管理员");
        }

        // 3. 检查是否已拥有商家角色
        UserRole existing = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
                        .eq(UserRole::getRoleId, sellerRole.getId())
        );
        if (existing != null) {
            throw new BusinessException("该用户已是商家，请勿重复操作");
        }

        // 4. 添加商家角色
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(sellerRole.getId());
        userRoleMapper.insert(userRole);
        log.info("商家角色添加成功: userId={}, roleId={}", userId, sellerRole.getId());

        // 5. 检查是否已有店铺（如果已有店铺则跳过创建）
        Store existingStore = storeMapper.selectBySellerId(userId);
        Long storeId = null;
        if (existingStore == null) {
            // 自动创建默认店铺
            Store store = new Store();
            store.setName(user.getRealName() != null ? user.getRealName() + "的店铺" : user.getUsername() + "的店铺");
            store.setSellerId(userId);
            store.setStatus(1); // 默认启用
            storeMapper.insert(store);
            storeId = store.getId();
            log.info("默认店铺创建成功: storeId={}, name={}", storeId, store.getName());
        } else {
            storeId = existingStore.getId();
            log.info("用户已有店铺，跳过创建: storeId={}", storeId);
        }

        // 6. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("roleId", sellerRole.getId());
        result.put("roleCode", sellerRole.getCode());
        result.put("roleName", sellerRole.getName());
        result.put("storeId", storeId);
        return result;
    }
}