package com.cyh.mallportal.service.impl;

import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallportal.dto.AddressDto;
import com.cyh.mallportal.entity.Address;
import com.cyh.mallportal.mapper.AddressMapper;
import com.cyh.mallportal.service.AddressService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.cyh.mallcommon.constant.RedisConstants;

/**
 * 收货地址服务实现类
 * 提供收货地址业务逻辑的具体实现
 * <p>
 * 缓存设计说明：
 * - 缓存 Key: address:list:{userId} → JSON (List<Address>)
 * - TTL: 1 小时
 * - 三个查询方法共享同一缓存，通过内存过滤获取所需数据
 * - 所有写操作（增/删/改/设默认）均清除该用户的缓存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /** 最大地址数量限制 */
    private static final int MAX_ADDRESS_COUNT = 10;

    // ==================== 带缓存的查询方法 ====================

    /**
     * 获取用户所有地址列表
     * <p>
     * 【执行流程】
     * Step1: 从 Redis 读取缓存（List<Address> 的 JSON）
     * Step2: 缓存命中 → 直接返回
     * Step3: 缓存未命中 → 查 MySQL 并写入缓存
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        // Step1: 尝试从缓存读取
        List<Address> cached = getAddressListFromCache(userId);
        if (cached != null) {
            return cached;
        }

        // Step2: 缓存未命中，查库
        List<Address> result = addressMapper.selectByUserId(userId);
        if (result == null) {
            result = new ArrayList<>();
        }

        // Step3: 写入缓存
        cacheAddressList(userId, result);

        return result;
    }

    /**
     * 获取用户默认地址
     * <p>
     * 【执行流程】
     * Step1: 从 Redis 读取缓存（共享 address:list:{userId} ）
     * Step2: 缓存命中 → 过滤出 isDefault==1 的地址
     * Step3: 缓存未命中 → 查 MySQL 并写入缓存，再过滤
     *
     * @param userId 用户ID
     * @return 默认地址（不存在则返回 null）
     */
    @Override
    public Address getDefaultAddress(Long userId) {
        // Step1: 尝试从缓存读取（共享地址列表缓存）
        List<Address> cached = getAddressListFromCache(userId);
        if (cached != null) {
            // 内存过滤默认地址
            return cached.stream()
                    .filter(a -> a.getIsDefault() != null && a.getIsDefault() == 1)
                    .findFirst().orElse(null);
        }

        // Step2: 缓存未命中，查库
        Address result = addressMapper.selectDefaultByUserId(userId);

        // Step3: 查全量列表写入缓存（确保下次其他查询也能命中）
        List<Address> allAddresses = addressMapper.selectByUserId(userId);
        cacheAddressList(userId, allAddresses != null ? allAddresses : new ArrayList<>());

        return result;
    }

    /**
     * 根据ID获取地址详情
     * <p>
     * 【执行流程】
     * Step1: 从 Redis 读取缓存（共享 address:list:{userId} ）
     * Step2: 缓存命中 → 过滤出匹配 id 的地址
     * Step3: 缓存未命中 → 查 MySQL 并写入缓存，再过滤
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 地址实体（不存在或不属于该用户则返回 null）
     */
    @Override
    public Address getAddressById(Long userId, Long addressId) {
        // Step1: 尝试从缓存读取
        List<Address> cached = getAddressListFromCache(userId);
        if (cached != null) {
            // 内存过滤指定地址
            return cached.stream()
                    .filter(a -> a.getId().equals(addressId))
                    .findFirst().orElse(null);
        }

        // Step2: 缓存未命中，查库
        Address result = addressMapper.selectById(addressId);
        if (result != null && !result.getUserId().equals(userId)) {
            result = null;
        }

        // Step3: 查全量列表写入缓存
        List<Address> allAddresses = addressMapper.selectByUserId(userId);
        cacheAddressList(userId, allAddresses != null ? allAddresses : new ArrayList<>());

        return result;
    }

    // ==================== 写操作（含缓存清除） ====================

    /**
     * 添加收货地址
     * <p>
     * 【执行流程】
     * Step1: 校验地址数量上限
     * Step2: 处理默认地址逻辑
     * Step3: 插入数据库
     * Step4: 清除该用户的地址列表缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAddress(Long userId, AddressDto addressDto) {
        log.info("添加收货地址, 用户ID: {}", userId);

        // 检查地址数量是否超过限制
        int count = addressMapper.countByUserId(userId);
        if (count >= MAX_ADDRESS_COUNT) {
            log.warn("地址数量已达上限: {}", MAX_ADDRESS_COUNT);
            throw new BusinessException("地址数量已达上限，最多可添加" + MAX_ADDRESS_COUNT + "个地址");
        }

        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName(addressDto.getReceiverName());
        address.setReceiverPhone(addressDto.getReceiverPhone());
        address.setProvince(addressDto.getProvince());
        address.setCity(addressDto.getCity());
        address.setDistrict(addressDto.getDistrict());
        address.setDetailAddress(addressDto.getDetailAddress());
        address.setZipCode(addressDto.getZipCode());
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());

        // 如果设为默认地址，先取消其他默认地址
        if (addressDto.getIsDefault() != null && addressDto.getIsDefault() == 1) {
            address.setIsDefault(1);
            addressMapper.cancelDefaultByUserId(userId);
        } else {
            address.setIsDefault(0);
        }

        addressMapper.insert(address);

        // 清除缓存
        clearAddressCache(userId);

        log.info("添加收货地址成功, 地址ID: {}", address.getId());
        return address.getId();
    }

    /**
     * 更新收货地址
     * <p>
     * 【执行流程】
     * Step1: 校验地址存在性和归属
     * Step2: 更新数据库
     * Step3: 清除该用户的地址列表缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(Long userId, AddressDto addressDto) {
        log.info("更新收货地址, 用户ID: {}, 地址ID: {}", userId, addressDto.getId());

        Address address = addressMapper.selectById(addressDto.getId());
        if (address == null) {
            log.warn("地址不存在: {}", addressDto.getId());
            return false;
        }

        // 验证地址归属
        if (!address.getUserId().equals(userId)) {
            log.warn("地址不属于当前用户, 用户ID: {}, 地址ID: {}", userId, addressDto.getId());
            return false;
        }

        address.setReceiverName(addressDto.getReceiverName());
        address.setReceiverPhone(addressDto.getReceiverPhone());
        address.setProvince(addressDto.getProvince());
        address.setCity(addressDto.getCity());
        address.setDistrict(addressDto.getDistrict());
        address.setDetailAddress(addressDto.getDetailAddress());
        address.setZipCode(addressDto.getZipCode());
        address.setUpdatedAt(LocalDateTime.now());

        // 如果设为默认地址，先取消其他默认地址
        if (addressDto.getIsDefault() != null && addressDto.getIsDefault() == 1) {
            address.setIsDefault(1);
            addressMapper.cancelDefaultByUserId(userId);
        } else {
            address.setIsDefault(0);
        }

        int rows = addressMapper.updateById(address);
        boolean success = rows > 0;
        if (success) {
            // 清除缓存
            clearAddressCache(userId);
            log.info("更新收货地址成功, 地址ID: {}", addressDto.getId());
        } else {
            log.warn("更新收货地址失败, 地址ID: {}", addressDto.getId());
        }

        return success;
    }

    /**
     * 删除收货地址
     * <p>
     * 【执行流程】
     * Step1: 校验地址存在性和归属
     * Step2: 删除数据库记录
     * Step3: 清除该用户的地址列表缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long userId, Long addressId) {
        log.info("删除收货地址, 用户ID: {}, 地址ID: {}", userId, addressId);

        Address address = addressMapper.selectById(addressId);
        if (address == null) {
            log.warn("地址不存在: {}", addressId);
            return false;
        }

        // 验证地址归属
        if (!address.getUserId().equals(userId)) {
            log.warn("地址不属于当前用户, 用户ID: {}, 地址ID: {}", userId, addressId);
            return false;
        }

        addressMapper.deleteById(addressId);

        // 清除缓存
        clearAddressCache(userId);

        log.info("删除收货地址成功, 地址ID: {}", addressId);
        return true;
    }

    /**
     * 设置默认地址
     * <p>
     * 【执行流程】
     * Step1: 校验地址存在性和归属
     * Step2: 取消原有默认 + 设置新的默认地址
     * Step3: 清除该用户的地址列表缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Long userId, Long addressId) {
        log.info("设置默认地址, 用户ID: {}, 地址ID: {}", userId, addressId);

        Address address = addressMapper.selectById(addressId);
        if (address == null) {
            log.warn("地址不存在: {}", addressId);
            return false;
        }

        // 验证地址归属
        if (!address.getUserId().equals(userId)) {
            log.warn("地址不属于当前用户, 用户ID: {}, 地址ID: {}", userId, addressId);
            return false;
        }

        // 取消其他默认地址
        addressMapper.cancelDefaultByUserId(userId);

        // 设置当前地址为默认
        address.setIsDefault(1);
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(address);

        // 清除缓存
        clearAddressCache(userId);

        log.info("设置默认地址成功, 地址ID: {}", addressId);
        return true;
    }

    /**
     * 获取用户地址数量
     * <p>
     * 注：直接查库，不缓存。由 addAddress 在写操作时调用，写操作自身会清除缓存。
     *
     * @param userId 用户ID
     * @return 地址数量
     */
    @Override
    public int countAddresses(Long userId) {
        return addressMapper.countByUserId(userId);
    }

    // ==================== 缓存私有方法 ====================

    /**
     * 从缓存中读取用户地址列表
     *
     * @param userId 用户ID
     * @return 地址列表（缓存未命中返回 null）
     */
    private List<Address> getAddressListFromCache(Long userId) {
        String key = RedisConstants.ADDRESS_LIST_CACHE_KEY + userId;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached == null) {
            return null;
        }
        try {
            return objectMapper.readValue(cached,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Address.class));
        } catch (JsonProcessingException e) {
            log.error("反序列化地址缓存失败, key: {}", key, e);
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    /**
     * 将用户地址列表写入缓存
     *
     * @param userId  用户ID
     * @param list    地址列表
     */
    private void cacheAddressList(Long userId, List<Address> list) {
        String key = RedisConstants.ADDRESS_LIST_CACHE_KEY + userId;
        try {
            String json = objectMapper.writeValueAsString(list);
            stringRedisTemplate.opsForValue().set(key, json, RedisConstants.ADDRESS_CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("序列化地址缓存失败, key: {}", key, e);
        }
    }

    /**
     * 清除用户地址列表缓存
     *
     * @param userId 用户ID
     */
    private void clearAddressCache(Long userId) {
        String key = RedisConstants.ADDRESS_LIST_CACHE_KEY + userId;
        stringRedisTemplate.delete(key);
        log.debug("清除地址缓存, key: {}", key);
    }
}