package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallportal.entity.Role;
import com.cyh.mallportal.entity.Store;
import com.cyh.mallportal.entity.StoreAdmin;
import com.cyh.mallportal.entity.UserRole;
import com.cyh.mallportal.mapper.OrderMapper;
import com.cyh.mallportal.mapper.RoleMapper;
import com.cyh.mallportal.mapper.StoreAdminMapper;
import com.cyh.mallportal.mapper.StoreMapper;
import com.cyh.mallportal.mapper.UserRoleMapper;
import com.cyh.mallportal.service.StoreService;
import com.cyh.mallportal.vo.StoreDetailVO;
import com.cyh.mallportal.vo.StoreVO;
import com.cyh.mallcommon.constant.MyConstants;
import com.cyh.mallcommon.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
     * 根据ID查询店铺详情（返回 StoreDetailVO）
     * 用于公开展示店铺详细信息，不包含内部管理字段
     *
     * @param id 店铺ID
     * @return 店铺详情 VO，店铺不存在返回 null
     */
    @Override
    public StoreDetailVO getDetailVO(Long id) {
        Store store = storeMapper.selectById(id);
        return StoreDetailVO.fromStore(store);
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
     * 分页查询公开店铺列表（仅 status=1）
     * 用于无需权限的店铺展示页面，返回 StoreVO
     *
     * @param keyword  搜索关键字，模糊搜索店铺名称
     * @param page     页码，从1开始
     * @param pageSize 每页数量
     * @return 店铺 VO 分页列表
     */
    @Override
    public List<StoreVO> getPageVO(String keyword, Integer page, Integer pageSize) {
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null ? pageSize : 10);
        int limit = pageSize != null ? pageSize : 10;
        return storeMapper.selectPageVO(keyword, offset, limit);
    }

    /**
     * 统计公开店铺数量（仅 status=1）
     *
     * @param keyword 搜索关键字
     * @return 店铺总数
     */
    @Override
    public int countPageVO(String keyword) {
        return storeMapper.countPageVO(keyword);
    }

    /**
     * 管理员分页查询店铺列表（多条件筛选）
     * 可查看全部状态的店铺，支持按店铺ID、名称关键字、商家ID、联系电话搜索
     *
     * @param id       店铺ID（精确匹配，可选）
     * @param keyword  店铺名称关键字（模糊匹配，可选）
     * @param status   店铺状态（可选，不传查全部）
     * @param sellerId 商家用户ID（精确匹配，可选）
     * @param phone    联系电话（模糊匹配，可选）
     * @param page     页码，从1开始
     * @param pageSize 每页数量
     * @return 店铺列表（完整 Store 实体）
     */
    @Override
    public List<Store> getAdminPage(Long id, String keyword, Integer status,
                                    Long sellerId, String phone,
                                    Integer page, Integer pageSize) {
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null ? pageSize : 10);
        int limit = pageSize != null ? pageSize : 10;
        return storeMapper.selectAdminPage(id, keyword, status, sellerId, phone, offset, limit);
    }

    /**
     * 管理员统计店铺总数（多条件筛选）
     *
     * @param id       店铺ID（精确匹配，可选）
     * @param keyword  店铺名称关键字（模糊匹配，可选）
     * @param status   店铺状态（可选）
     * @param sellerId 商家用户ID（精确匹配，可选）
     * @param phone    联系电话（模糊匹配，可选）
     * @return 店铺总数
     */
    @Override
    public int countAdminPage(Long id, String keyword, Integer status,
                              Long sellerId, String phone) {
        return storeMapper.countAdminPage(id, keyword, status, sellerId, phone);
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

    // ========== 开店申请实现 ==========

    /**
     * 用户提交开店申请
     * 校验当前用户是否已有SELLER角色、是否有审核中的申请、店铺名是否唯一
     * 校验通过后创建status=2(审核中)的店铺
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Store apply(Long userId, String name, String description, String phone, String address) {
        // 1. 校验用户是否已有店铺（通过seller_id唯一索引保证，但先做友好提示）
        Store existStore = storeMapper.selectBySellerId(userId);
        if (existStore != null) {
            if (existStore.getStatus() == 1) {
                throw new BusinessException("您已拥有正常店铺，不可重复申请");
            }
            if (existStore.getStatus() == 2) {
                throw new BusinessException("您已提交开店申请，请等待审核");
            }
        }

        // 2. 校验店铺名称唯一性
        LambdaQueryWrapper<Store> nameQuery = new LambdaQueryWrapper<>();
        nameQuery.eq(Store::getName, name);
        if (storeMapper.selectCount(nameQuery) > 0) {
            throw new BusinessException("店铺名称已被占用");
        }

        // 3. 创建审核中的店铺
        Store store = new Store();
        store.setSellerId(userId);
        store.setName(name);
        store.setDescription(description);
        store.setPhone(phone);
        store.setAddress(address);
        store.setStatus(2);          // 审核中
        store.setSort(0);
        store.setCreatedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.insert(store);

        return store;
    }

    /**
     * 管理员审核通过开店申请
     * 将店铺status更新为1(正常)，并给用户赋予SELLER角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw new BusinessException("店铺不存在");
        }
        if (store.getStatus() != 2) {
            throw new BusinessException("该店铺不在审核中状态，无法审核通过");
        }

        // 1. 更新店铺状态为正常
        store.setStatus(1);
        store.setRejectReason(null);
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.updateById(store);

        // 2. 查询SELLER角色ID
        var sellerRole = roleMapper.selectByCode("SELLER");
        if (sellerRole == null) {
            throw new BusinessException("SELLER角色不存在，请检查数据库roles表");
        }

        // 3. 检查用户是否已有SELLER角色
        LambdaQueryWrapper<UserRole> roleCheck = new LambdaQueryWrapper<>();
        roleCheck.eq(UserRole::getUserId, store.getSellerId());
        roleCheck.eq(UserRole::getRoleId, sellerRole.getId());
        if (userRoleMapper.selectCount(roleCheck) == 0) {
            // 没有则赋予SELLER角色
            UserRole userRole = new UserRole();
            userRole.setUserId(store.getSellerId());
            userRole.setRoleId(sellerRole.getId());
            userRoleMapper.insert(userRole);
        }
    }

    /**
     * 管理员驳回开店申请
     * 将店铺status更新为3(审核失败)，记录驳回原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String rejectReason) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw new BusinessException("店铺不存在");
        }
        if (store.getStatus() != 2) {
            throw new BusinessException("该店铺不在审核中状态，无法驳回");
        }

        store.setStatus(3);          // 审核失败
        store.setRejectReason(rejectReason);
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.updateById(store);
    }

    /**
     * 用户重新提交开店申请（驳回后修改重新提交）
     * 校验用户有status=3(审核失败)的店铺，更新信息并重置status=2(审核中)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reSubmit(Long storeId, String name, String description, String phone, String address) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            throw new BusinessException("店铺不存在");
        }
        if (store.getStatus() != 3) {
            throw new BusinessException("仅审核失败的店铺可以重新提交");
        }

        // 如果改了店铺名，校验唯一性
        if (name != null && !name.equals(store.getName())) {
            LambdaQueryWrapper<Store> nameQuery = new LambdaQueryWrapper<>();
            nameQuery.eq(Store::getName, name);
            if (storeMapper.selectCount(nameQuery) > 0) {
                throw new BusinessException("店铺名称已被占用");
            }
            store.setName(name);
        }

        store.setDescription(description);
        store.setPhone(phone);
        store.setAddress(address);
        store.setStatus(2);            // 重新回到审核中
        store.setRejectReason(null);   // 清空驳回原因
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.updateById(store);
    }

    /**
     * 用户查看自己的开店申请
     */
    @Override
    public Store getMyApply(Long userId) {
        return storeMapper.selectBySellerId(userId);
    }

    /**
     * 管理员分页查询待审核列表（status=2）
     */
    @Override
    public List<Store> getPendingPage(Integer page, Integer pageSize) {
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null ? pageSize : 10);
        int limit = pageSize != null ? pageSize : 10;
        return storeMapper.selectPage(null, 2, offset, limit);
    }

    /**
     * 统计待审核数量
     */
    @Override
    public int countPending() {
        return storeMapper.countPage(null, 2);
    }

    // ========== 店铺注销实现 ==========

    /**
     * 商家注销店铺
     * 校验店铺下所有订单均为完结状态（已完成/已取消/已退款）后方可注销，
     * 注销后店铺 status 置为 0（禁用/已注销）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivate(Long storeId, Long userId) {
        // 1. 校验店铺存在且属于当前用户
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            throw new BusinessException("店铺不存在");
        }
        if (!store.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作该店铺");
        }
        if (store.getStatus() == 0) {
            throw new BusinessException("该店铺已注销");
        }

        // 2. 校验店铺下是否有进行中的订单（1-待付款 2-待发货 3-待收货 6-退款中 8-已拒绝）
        int activeOrderCount = orderMapper.countActiveBySellerId(userId);
        if (activeOrderCount > 0) {
            throw new BusinessException(
                String.format("该店铺还有 %d 笔进行中的订单，请处理完毕后（订单状态为已完成/已取消/已退款）再注销", activeOrderCount));
        }

        // 3. 更新店铺状态为 0（禁用/已注销）
        store.setStatus(0);
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.updateById(store);

        // 4. 移除用户的 SELLER 角色
        Role sellerRole = roleMapper.selectByCode("SELLER");
        if (sellerRole != null) {
            LambdaQueryWrapper<UserRole> roleRemove = new LambdaQueryWrapper<>();
            roleRemove.eq(UserRole::getUserId, userId);
            roleRemove.eq(UserRole::getRoleId, sellerRole.getId());
            userRoleMapper.delete(roleRemove);
        }

        // 5. 更新 Redis 缓存：移除 SELLER 角色
        String token = (String) redisTemplate.opsForValue()
                .get(MyConstants.USER_ACTIVE_TOKEN_PREFIX + userId);
        if (token != null) {
            String tokenKey = MyConstants.TOKEN_PREFIX + token;
            Object userInfoObj = redisTemplate.opsForValue().get(tokenKey);
            if (userInfoObj instanceof Map) {

                Map<String, Object> userInfo = (Map<String, Object>) userInfoObj;

                // 从 roles 中移除 SELLER
                Map<String, String> roles = (Map<String, String>) userInfo.get("roles");
                if (roles != null) {
                    roles.remove("SELLER");
                }

                // 写回 Redis，保持原过期时间
                redisTemplate.opsForValue().set(
                        tokenKey, userInfo,
                        MyConstants.TOKEN_EXPIRATION, TimeUnit.SECONDS
                );
            }
        }
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
