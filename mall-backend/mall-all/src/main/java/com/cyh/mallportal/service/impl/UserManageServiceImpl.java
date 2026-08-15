package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallcommon.constant.RedisConstants;
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
import com.cyh.mallportal.vo.PromoteToSellerVo;
import com.cyh.mallportal.vo.UserManageVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理 Service 实现类（后台管理用）
 *
 * 提供超级管理员对用户的管理功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManageServiceImpl implements UserManageService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final StoreMapper storeMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 商家角色的编码（从数据库 roles 表获取）
     */
    private static final String SELLER_ROLE_CODE = "SELLER";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromoteToSellerVo promoteToSeller(Long userId) {
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
        PromoteToSellerVo vo = new PromoteToSellerVo();
        vo.setUserId(userId);
        vo.setRoleId(sellerRole.getId());
        vo.setRoleCode(sellerRole.getCode());
        vo.setRoleName(sellerRole.getName());
        vo.setStoreId(storeId);
        return vo;
    }

    /**
     * 分页查询用户列表
     * 支持按用户名/手机号/邮箱模糊搜索，结果附带用户角色信息
     */
    @Override
    public IPage<UserManageVo> pageUsers(Integer page, Integer pageSize, String keyword,
                                          Integer status, String roleCode,
                                          LocalDateTime registerStartTime, LocalDateTime registerEndTime) {
        // 1. 构建分页对象
        Page<User> pageObj = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);

        // 2. 构建查询条件：只查未逻辑删除的用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNull(User::getDeletedAt);

        // 3. 关键词模糊搜索（用户名/手机号/邮箱）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getPhone, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }

        // 4. 按用户状态筛选
        if (status != null && (status == 0 || status == 1)) {
            queryWrapper.eq(User::getStatus, status);
        }

        // 5. 按角色编码筛选（子查询：user_roles → roles）
        if (StringUtils.hasText(roleCode)) {
            queryWrapper.inSql(User::getId,
                    "SELECT user_id FROM user_roles ur " +
                    "INNER JOIN roles r ON ur.role_id = r.id " +
                    "WHERE r.code = '" + roleCode + "'");
        }

        // 6. 按注册时间范围筛选
        if (registerStartTime != null) {
            queryWrapper.ge(User::getCreatedAt, registerStartTime);
        }
        if (registerEndTime != null) {
            queryWrapper.le(User::getCreatedAt, registerEndTime);
        }

        // 7. 按创建时间倒序排列
        queryWrapper.orderByDesc(User::getCreatedAt);

        // 8. 执行分页查询
        IPage<User> userPage = userMapper.selectPage(pageObj, queryWrapper);

        // 9. 转换为 UserManageVo
        Page<UserManageVo> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserManageVo> voList = userPage.getRecords().stream()
                .map(user -> {
                    List<Role> roles = roleMapper.selectByUserId(user.getId());
                    List<String> roleCodes = roles.stream().map(Role::getCode).toList();
                    return UserManageVo.fromUser(user, roleCodes);
                })
                .toList();
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 启用用户
     * 将用户状态设为 1-启用，用户可正常登录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(Long userId) {
        // 1. 校验用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 如果已是启用状态则提示
        if (user.getStatus() != null && user.getStatus().equals(1)) {
            throw new BusinessException("该用户已是启用状态");
        }

        // 3. 更新用户状态
        user.setStatus(1);
        userMapper.updateById(user);

        log.info("用户已启用: userId={}", userId);
    }

    /**
     * 禁用用户
     * 将用户状态设为 0-禁用，禁用后用户无法登录系统，
     * 同时清除 Redis 中的 Token 缓存使其立即下线
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long userId) {
        // 1. 校验用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 如果已是禁用状态则提示
        if (user.getStatus() != null && user.getStatus().equals(0)) {
            throw new BusinessException("该用户已是禁用状态");
        }

        // 3. 更新用户状态
        user.setStatus(0);
        userMapper.updateById(user);

        // 4. 清除 Redis 中的 Token 缓存使其立即下线
        String activeToken = (String) redisTemplate.opsForValue().get(
                RedisConstants.USER_ACTIVE_TOKEN_PREFIX + userId);
        if (activeToken != null) {
            redisTemplate.delete(RedisConstants.TOKEN_PREFIX + activeToken);
            redisTemplate.delete(RedisConstants.USER_CURRENT_SESSION_PREFIX + userId);
            redisTemplate.delete(RedisConstants.USER_ACTIVE_TOKEN_PREFIX + userId);
            log.info("用户被禁用，已清除登录缓存: userId={}", userId);
        }

        log.info("用户已禁用: userId={}", userId);
    }
}