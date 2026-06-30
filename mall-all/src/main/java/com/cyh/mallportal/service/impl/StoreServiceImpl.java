package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyh.mallportal.entity.Role;
import com.cyh.mallportal.entity.Spu;
import com.cyh.mallportal.entity.Store;
import com.cyh.mallportal.entity.StoreAdmin;
import com.cyh.mallportal.entity.UserRole;
import com.cyh.mallportal.mapper.OrderMapper;
import com.cyh.mallportal.mapper.RoleMapper;
import com.cyh.mallportal.mapper.SpuMapper;
import com.cyh.mallportal.mapper.StoreAdminMapper;
import com.cyh.mallportal.mapper.StoreMapper;
import com.cyh.mallportal.mapper.UserRoleMapper;
import com.cyh.mallportal.service.StoreService;
import com.cyh.mallportal.vo.StoreDetailVo;
import com.cyh.mallportal.vo.StoreVo;
import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallportal.mq.event.CacheDomain;
import com.cyh.mallportal.mq.event.CacheInvalidateEvent;
import com.cyh.mallportal.mq.publisher.CacheEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 店铺服务实现类
 * 提供店铺创建、修改、查询、权限管理等功能
 */
@Slf4j
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
    private SpuMapper spuMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheEventPublisher cacheEventPublisher;

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
     * 自动更新修改时间，事务提交后异步清除缓存
     *
     * @param store 待更新的店铺信息
     * @return 更新成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Store store) {
        store.setUpdatedAt(LocalDateTime.now());
        boolean success = storeMapper.updateById(store) > 0;

        if (success && store.getId() != null) {
            // 事务提交后，异步清除缓存
            publishStoreCacheInvalidate(store.getId());
        }

        return success;
    }

    /**
     * 根据ID查询店铺（Cache-Aside 模式）
     * Step1: 查缓存 store:{id}
     * Step2: 命中直接返回，未命中查 DB 并回写缓存
     *
     * @param id 店铺ID
     * @return 店铺实体，不存在返回null
     */
    @Override
    public Store getById(Long id) {
        // Step1: 查缓存
        String cacheKey = RedisConstants.STORE_CACHE_KEY + id;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, Store.class);
            } catch (JsonProcessingException e) {
                log.warn("反序列化店铺缓存失败, key: {}", cacheKey, e);
                stringRedisTemplate.delete(cacheKey);
            }
        }

        // Step2: 缓存未命中，查库
        Store store = storeMapper.selectById(id);

        // Step3: 回写缓存
        if (store != null) {
            try {
                String json = objectMapper.writeValueAsString(store);
                stringRedisTemplate.opsForValue().set(cacheKey, json, RedisConstants.STORE_CACHE_TTL_HOURS, TimeUnit.HOURS);
            } catch (JsonProcessingException e) {
                log.warn("序列化店铺缓存失败, key: {}", cacheKey, e);
            }
        }

        return store;
    }

    /**
     * 根据ID查询店铺详情（Cache-Aside 模式）
     * 返回 StoreDetailVo，用于公开展示，不包含内部管理字段
     * 缓存 Key: store:detail:{id}，TTL: 1 小时
     *
     * @param id 店铺ID
     * @return 店铺详情 VO，店铺不存在返回 null
     */
    @Override
    public StoreDetailVo getDetailVO(Long id) {
        // Step1: 查缓存
        String cacheKey = RedisConstants.STORE_DETAIL_KEY + id;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, StoreDetailVo.class);
            } catch (JsonProcessingException e) {
                log.warn("反序列化店铺详情缓存失败, key: {}", cacheKey, e);
                stringRedisTemplate.delete(cacheKey);
            }
        }

        // Step2: 缓存未命中，查库
        Store store = storeMapper.selectById(id);
        StoreDetailVo vo = StoreDetailVo.fromStore(store);

        // Step3: 回写缓存
        if (vo != null) {
            try {
                String json = objectMapper.writeValueAsString(vo);
                stringRedisTemplate.opsForValue().set(cacheKey, json, RedisConstants.STORE_CACHE_TTL_HOURS, TimeUnit.HOURS);
            } catch (JsonProcessingException e) {
                log.warn("序列化店铺详情缓存失败, key: {}", cacheKey, e);
            }
        }

        return vo;
    }

    /**
     * 根据商家用户ID查询店铺（Cache-Aside 模式）
     * 缓存 Key: store:seller:{sellerId}，TTL: 1 小时
     *
     * @param sellerId 商家用户ID
     * @return 店铺实体，不存在返回null
     */
    @Override
    public Store getBySellerId(Long sellerId) {
        // Step1: 查缓存
        String cacheKey = RedisConstants.STORE_SELLER_KEY + sellerId;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, Store.class);
            } catch (JsonProcessingException e) {
                log.warn("反序列化商家店铺映射缓存失败, key: {}", cacheKey, e);
                stringRedisTemplate.delete(cacheKey);
            }
        }

        // Step2: 缓存未命中，查库
        Store store = storeMapper.selectBySellerId(sellerId);

        // Step3: 回写缓存
        if (store != null) {
            try {
                String json = objectMapper.writeValueAsString(store);
                stringRedisTemplate.opsForValue().set(cacheKey, json, RedisConstants.STORE_CACHE_TTL_HOURS, TimeUnit.HOURS);
            } catch (JsonProcessingException e) {
                log.warn("序列化商家店铺映射缓存失败, key: {}", cacheKey, e);
            }
        }

        return store;
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
     * 用于无需权限的店铺展示页面，返回 StoreVo
     *
     * @param keyword  搜索关键字，模糊搜索店铺名称
     * @param page     页码，从1开始
     * @param pageSize 每页数量
     * @return 店铺 VO 分页列表
     */
    @Override
    public List<StoreVo> getPageVO(String keyword, Integer page, Integer pageSize) {
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
     * 同时自动更新修改时间，事务提交后异步清除缓存
     *
     * @param id 店铺ID
     * @param status 新状态
     * @return 更新成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Store store = new Store();
        store.setId(id);
        store.setStatus(status);
        store.setUpdatedAt(LocalDateTime.now());
        boolean success = storeMapper.updateById(store) > 0;

        if (success && id != null) {
            // 事务提交后，异步清除缓存
            publishStoreCacheInvalidate(id);
        }

        return success;
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
        // 1. 校验用户是否已有店铺
        Store existStore = storeMapper.selectBySellerId(userId);
        if (existStore != null) {
            if (existStore.getStatus() == 1) {
                throw new BusinessException("您已拥有正常店铺，不可重复申请");
            }
            if (existStore.getStatus() == 2) {
                throw new BusinessException("您已提交开店申请，请等待审核");
            }
            if (existStore.getStatus() == 0) {
                throw new BusinessException("您的店铺已注销，如需重新开店请使用重新申请功能");
            }
            // status == 3（审核失败）
            throw new BusinessException("您的开店申请已被驳回，请使用重新提交功能");
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
            userRole.setCreatedAt(LocalDateTime.now());
            userRoleMapper.insert(userRole);
        }

        // 事务提交后，异步清除缓存
        publishStoreCacheInvalidate(id);
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

        // 事务提交后，异步清除缓存
        publishStoreCacheInvalidate(id);
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

        // 事务提交后，异步清除缓存
        publishStoreCacheInvalidate(storeId);
    }

    /**
     * 已注销店铺重新申请开店
     * 校验用户有status=0(已注销)的店铺，更新信息并重置status=2(审核中)
     * 店铺名称修改时需校验唯一性（排除自身）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reApply(Long storeId, Long userId, String name, String description, String phone, String address) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            throw new BusinessException("店铺不存在");
        }
        if (store.getStatus() != 0) {
            throw new BusinessException("仅已注销的店铺可以重新申请");
        }
        if (!store.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作该店铺");
        }

        // 如果改了店铺名，校验唯一性（排除自身）
        if (name != null && !name.equals(store.getName())) {
            LambdaQueryWrapper<Store> nameQuery = new LambdaQueryWrapper<>();
            nameQuery.eq(Store::getName, name)
                     .ne(Store::getId, storeId);
            if (storeMapper.selectCount(nameQuery) > 0) {
                throw new BusinessException("店铺名称已被占用");
            }
            store.setName(name);
        }

        store.setDescription(description);
        store.setPhone(phone);
        store.setAddress(address);
        store.setStatus(2);            // 重新进入审核中
        store.setRejectReason(null);   // 清空驳回原因
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.updateById(store);

        // 事务提交后，异步清除缓存
        publishStoreCacheInvalidate(storeId);
    }

    /**
     * 用户查看自己的开店申请
     */
    @Override
    public Store getMyApply(Long userId) {
        return storeMapper.selectBySellerId(userId);
    }

    /**
     * 管理员分页查询指定审核状态的店铺申请列表
     * 复用通用 selectPage 方法，通过 status 精确筛选
     */
    @Override
    public List<Store> getPendingPage(Integer status, Integer page, Integer pageSize) {
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null ? pageSize : 10);
        int limit = pageSize != null ? pageSize : 10;
        return storeMapper.selectPage(null, status, offset, limit);
    }

    /**
     * 统计指定审核状态的店铺申请数量
     * 复用通用 countPage 方法
     */
    @Override
    public int countPending(Integer status) {
        return storeMapper.countPage(null, status);
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

        // 4. 将该店铺下的所有 SPU 批量下架
        Spu spuUpdate = new Spu();
        spuUpdate.setStatus(0);
        spuMapper.update(spuUpdate,
                Wrappers.<Spu>lambdaUpdate().eq(Spu::getSellerId, userId));

        // 5. 移除用户的 SELLER 角色
        Role sellerRole = roleMapper.selectByCode("SELLER");
        if (sellerRole != null) {
            LambdaQueryWrapper<UserRole> roleRemove = new LambdaQueryWrapper<>();
            roleRemove.eq(UserRole::getUserId, userId);
            roleRemove.eq(UserRole::getRoleId, sellerRole.getId());
            userRoleMapper.delete(roleRemove);
        }

        // 6. 更新 Redis 缓存：移除 SELLER 角色
        String token = (String) redisTemplate.opsForValue()
                .get(RedisConstants.USER_ACTIVE_TOKEN_PREFIX + userId);
        if (token != null) {
            String tokenKey = RedisConstants.TOKEN_PREFIX + token;
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
                        RedisConstants.TOKEN_EXPIRATION, TimeUnit.SECONDS
                );
            }
        }

        // 事务提交后，异步清除店铺缓存
        publishStoreCacheInvalidate(storeId);
    }

    /**
     * 事务提交后，异步发布店铺缓存失效事件（通过 storeId 回查 sellerId）
     * <p>
     * 精确删除 store:{id}、store:detail:{id}、store:seller:{sellerId} 三个 key
     */
    private void publishStoreCacheInvalidate(Long storeId) {
        if (storeId == null) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                List<String> keys = new ArrayList<>();
                keys.add(RedisConstants.STORE_CACHE_KEY + storeId);
                keys.add(RedisConstants.STORE_DETAIL_KEY + storeId);
                // 从 DB 回查最新 sellerId，清除 seller 映射缓存
                Store current = storeMapper.selectById(storeId);
                if (current != null && current.getSellerId() != null) {
                    keys.add(RedisConstants.STORE_SELLER_KEY + current.getSellerId());
                }
                cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                        .setDomain(CacheDomain.STORE)
                        .setExactKeys(keys));
                log.debug("发布店铺缓存失效事件, storeId: {}, keys: {}", storeId, keys);
            }
        });
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
