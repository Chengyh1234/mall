package com.cyh.mallstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallcommon.utils.RedisUtils;
import com.cyh.mallstore.entity.Store;
import com.cyh.mallstore.entity.StoreAdmin;
import com.cyh.mallstore.mapper.StoreAdminMapper;
import com.cyh.mallstore.mapper.StoreMapper;
import com.cyh.mallstore.service.StoreService;
import com.cyh.mallstore.vo.StoreDetailVo;
import com.cyh.mallstore.vo.StoreVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 店铺服务实现类
 * 实现店铺的完整 CRUD、开店申请/审核流程、Redis 缓存管理
 * 缓存策略：按店铺 ID、商家 ID、详情 VO 分别缓存，写操作时主动清除
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreMapper storeMapper;
    private final StoreAdminMapper storeAdminMapper;
    private final RedisUtils redisUtils;

    @Override
    public Long add(Store store) {
        // 设置创建时间和更新时间，默认状态为正常（1），默认排序为 0
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Store store) {
        // 更新店铺信息并清除相关缓存，确保下次读取时获取最新数据
        store.setUpdatedAt(LocalDateTime.now());
        boolean success = storeMapper.updateById(store) > 0;
        if (success && store.getId() != null) {
            deleteStoreCache(store.getId());
        }
        return success;
    }

    @Override
    public Store getById(Long id) {
        // 缓存读取策略：优先查 Redis，缓存未命中则查库并回填，缓存过期时间 24 小时
        String cacheKey = RedisConstants.STORE_CACHE_KEY + id;
        Store cached = redisUtils.getObject(cacheKey, Store.class);
        if (cached != null) {
            return cached;
        }
        Store store = storeMapper.selectById(id);
        if (store != null) {
            redisUtils.setObject(cacheKey, store, RedisConstants.STORE_CACHE_TTL_HOURS, TimeUnit.HOURS);
        }
        return store;
    }

    @Override
    public StoreDetailVo getDetailVO(Long id) {
        // 店铺详情 VO 缓存，与店铺实体缓存独立，避免查询详情时返回过多字段
        String cacheKey = RedisConstants.STORE_DETAIL_KEY + id;
        StoreDetailVo cached = redisUtils.getObject(cacheKey, StoreDetailVo.class);
        if (cached != null) {
            return cached;
        }
        Store store = storeMapper.selectById(id);
        StoreDetailVo vo = StoreDetailVo.fromStore(store);
        if (vo != null) {
            redisUtils.setObject(cacheKey, vo, RedisConstants.STORE_CACHE_TTL_HOURS, TimeUnit.HOURS);
        }
        return vo;
    }

    @Override
    public Store getBySellerId(Long sellerId) {
        // 通过卖家 ID 查询店铺，缓存 sellerId → store 的映射关系
        String cacheKey = RedisConstants.STORE_SELLER_KEY + sellerId;
        Store cached = redisUtils.getObject(cacheKey, Store.class);
        if (cached != null) {
            return cached;
        }
        Store store = storeMapper.selectBySellerId(sellerId);
        if (store != null) {
            redisUtils.setObject(cacheKey, store, RedisConstants.STORE_CACHE_TTL_HOURS, TimeUnit.HOURS);
        }
        return store;
    }

    @Override
    public List<Store> getList(Store store) {
        LambdaQueryWrapper<Store> queryWrapper = buildQueryWrapper(store);
        queryWrapper.orderByDesc(Store::getCreatedAt);
        return storeMapper.selectList(queryWrapper);
    }

    @Override
    public List<Store> getPage(String keyword, Integer status, Integer page, Integer pageSize) {
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null ? pageSize : 10);
        int limit = pageSize != null ? pageSize : 10;
        return storeMapper.selectPage(keyword, status, offset, limit);
    }

    @Override
    public int countPage(String keyword, Integer status) {
        return storeMapper.countPage(keyword, status);
    }

    @Override
    public List<StoreVo> getPageVO(String keyword, Integer page, Integer pageSize) {
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null ? pageSize : 10);
        int limit = pageSize != null ? pageSize : 10;
        return storeMapper.selectPageVO(keyword, offset, limit);
    }

    @Override
    public int countPageVO(String keyword) {
        return storeMapper.countPageVO(keyword);
    }

    @Override
    public List<Store> getAdminPage(Long id, String keyword, Integer status,
                                    Long sellerId, String phone,
                                    Integer page, Integer pageSize) {
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null ? pageSize : 10);
        int limit = pageSize != null ? pageSize : 10;
        return storeMapper.selectAdminPage(id, keyword, status, sellerId, phone, offset, limit);
    }

    @Override
    public int countAdminPage(Long id, String keyword, Integer status,
                              Long sellerId, String phone) {
        return storeMapper.countAdminPage(id, keyword, status, sellerId, phone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Store store = new Store();
        store.setId(id);
        store.setStatus(status);
        store.setUpdatedAt(LocalDateTime.now());
        boolean success = storeMapper.updateById(store) > 0;
        if (success && id != null) {
            deleteStoreCache(id);
        }
        return success;
    }

    @Override
    public boolean isStoreOwner(Long storeId, Long userId) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            return false;
        }
        return store.getSellerId() != null && store.getSellerId().equals(userId);
    }

    @Override
    public boolean isStoreAdmin(Long storeId, Long userId) {
        StoreAdmin admin = storeAdminMapper.selectByStoreIdAndUserId(storeId, userId);
        return admin != null && admin.getStatus() != null && admin.getStatus() == 1;
    }

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
     * 校验逻辑：用户是否有正常店铺 / 是否已有申请 / 是否已注销 / 是否已驳回
     * 店铺名称不能重复，申请后状态为"审核中（status=2）"
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Store apply(Long userId, String name, String description, String phone, String address) {
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
            throw new BusinessException("您的开店申请已被驳回，请使用重新提交功能");
        }

        LambdaQueryWrapper<Store> nameQuery = new LambdaQueryWrapper<>();
        nameQuery.eq(Store::getName, name);
        if (storeMapper.selectCount(nameQuery) > 0) {
            throw new BusinessException("店铺名称已被占用");
        }

        Store store = new Store();
        store.setSellerId(userId);
        store.setName(name);
        store.setDescription(description);
        store.setPhone(phone);
        store.setAddress(address);
        store.setStatus(2);
        store.setSort(0);
        store.setCreatedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.insert(store);
        return store;
    }

    /**
     * 管理员审核通过，将店铺状态从"审核中（2）"改为"正常（1）"
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
        store.setStatus(1);
        store.setRejectReason(null);
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.updateById(store);
        deleteStoreCache(id);
    }

    /**
     * 管理员驳回开店申请，记录驳回原因，状态改为"审核失败（3）"
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
        store.setStatus(3);
        store.setRejectReason(rejectReason);
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.updateById(store);
        deleteStoreCache(id);
    }

    /**
     * 审核失败后重新提交，仅 status=3 的店铺可操作，支持修改名称
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
        store.setStatus(2);
        store.setRejectReason(null);
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.updateById(store);
        deleteStoreCache(storeId);
    }

    /**
     * 已注销店铺重新申请开店，仅 status=0 的店铺可操作，校验用户是否为店铺所有者
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
        store.setStatus(2);
        store.setRejectReason(null);
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.updateById(store);
        deleteStoreCache(storeId);
    }

    @Override
    public Store getMyApply(Long userId) {
        return storeMapper.selectBySellerId(userId);
    }

    @Override
    public List<Store> getPendingPage(Integer status, Integer page, Integer pageSize) {
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null ? pageSize : 10);
        int limit = pageSize != null ? pageSize : 10;
        return storeMapper.selectPage(null, status, offset, limit);
    }

    @Override
    public int countPending(Integer status) {
        return storeMapper.countPage(null, status);
    }

    /**
     * 用户主动注销店铺，将状态改为"已注销（0）"，校验用户是否为店铺所有者
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivate(Long storeId, Long userId) {
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
        store.setStatus(0);
        store.setUpdatedAt(LocalDateTime.now());
        storeMapper.updateById(store);
        deleteStoreCache(storeId);
    }

    /**
     * 清除店铺相关所有缓存（店铺实体缓存、详情 VO 缓存、卖家映射缓存）
     * 在店铺信息变更时调用，确保缓存一致性
     */
    private void deleteStoreCache(Long storeId) {
        if (storeId == null) {
            return;
        }
        redisUtils.delete(RedisConstants.STORE_CACHE_KEY + storeId);
        redisUtils.delete(RedisConstants.STORE_DETAIL_KEY + storeId);
        Store current = storeMapper.selectById(storeId);
        if (current != null && current.getSellerId() != null) {
            redisUtils.delete(RedisConstants.STORE_SELLER_KEY + current.getSellerId());
        }
    }

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