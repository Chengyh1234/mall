package com.cyh.malluser.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallcommon.utils.RedisUtils;
import com.cyh.malluser.dto.AddressDto;
import com.cyh.malluser.entity.Address;
import com.cyh.malluser.mapper.AddressMapper;
import com.cyh.malluser.service.AddressService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 收货地址服务实现类
 * <p>
 * 缓存设计说明：
 * - 缓存Key: address:list:{userId} → JSON (List<Address>)
 * - TTL: 24 小时
 * - 三个查询方法共享同一缓存，通过内存过滤获取所需数据
 * - 所有写操作（增/删/改/设默认）均清除该用户的缓存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;
    private final RedisUtils redisUtils;

    /** 最大地址数量限制 */
    private static final int MAX_ADDRESS_COUNT = 10;

    // ==================== 带缓存的查询方法 ====================

    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        List<Address> cached = getAddressListFromCache(userId);
        if (cached != null) {
            return cached;
        }

        List<Address> result = addressMapper.selectByUserId(userId);
        if (result == null) {
            result = new ArrayList<>();
        }
        cacheAddressList(userId, result);
        return result;
    }

    @Override
    public Address getDefaultAddress(Long userId) {
        List<Address> cached = getAddressListFromCache(userId);
        if (cached != null) {
            return cached.stream()
                    .filter(a -> a.getIsDefault() != null && a.getIsDefault() == 1)
                    .findFirst().orElse(null);
        }

        Address result = addressMapper.selectDefaultByUserId(userId);

        List<Address> allAddresses = addressMapper.selectByUserId(userId);
        cacheAddressList(userId, allAddresses != null ? allAddresses : new ArrayList<>());

        return result;
    }

    @Override
    public Address getAddressById(Long userId, Long addressId) {
        List<Address> cached = getAddressListFromCache(userId);
        if (cached != null) {
            return cached.stream()
                    .filter(a -> a.getId().equals(addressId))
                    .findFirst().orElse(null);
        }

        Address result = addressMapper.selectById(addressId);
        if (result != null && !result.getUserId().equals(userId)) {
            result = null;
        }

        List<Address> allAddresses = addressMapper.selectByUserId(userId);
        cacheAddressList(userId, allAddresses != null ? allAddresses : new ArrayList<>());

        return result;
    }

    // ==================== 写操作（含缓存清除） ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAddress(Long userId, AddressDto addressDto) {
        log.info("添加收货地址, 用户ID: {}", userId);

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

        if (addressDto.getIsDefault() != null && addressDto.getIsDefault() == 1) {
            address.setIsDefault(1);
            addressMapper.cancelDefaultByUserId(userId);
        } else {
            address.setIsDefault(0);
        }

        addressMapper.insert(address);
        clearAddressCache(userId);

        log.info("添加收货地址成功, 地址ID: {}", address.getId());
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(Long userId, AddressDto addressDto) {
        log.info("更新收货地址, 用户ID: {}, 地址ID: {}", userId, addressDto.getId());

        Address address = addressMapper.selectById(addressDto.getId());
        if (address == null) {
            log.warn("地址不存在: {}", addressDto.getId());
            return false;
        }

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

        if (addressDto.getIsDefault() != null && addressDto.getIsDefault() == 1) {
            address.setIsDefault(1);
            addressMapper.cancelDefaultByUserId(userId);
        } else {
            address.setIsDefault(0);
        }

        int rows = addressMapper.updateById(address);
        boolean success = rows > 0;
        if (success) {
            clearAddressCache(userId);
            log.info("更新收货地址成功, 地址ID: {}", addressDto.getId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long userId, Long addressId) {
        log.info("删除收货地址, 用户ID: {}, 地址ID: {}", userId, addressId);

        Address address = addressMapper.selectById(addressId);
        if (address == null) {
            log.warn("地址不存在: {}", addressId);
            return false;
        }

        if (!address.getUserId().equals(userId)) {
            log.warn("地址不属于当前用户, 用户ID: {}, 地址ID: {}", userId, addressId);
            return false;
        }

        addressMapper.deleteById(addressId);
        clearAddressCache(userId);

        log.info("删除收货地址成功, 地址ID: {}", addressId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Long userId, Long addressId) {
        log.info("设置默认地址, 用户ID: {}, 地址ID: {}", userId, addressId);

        Address address = addressMapper.selectById(addressId);
        if (address == null) {
            log.warn("地址不存在: {}", addressId);
            return false;
        }

        if (!address.getUserId().equals(userId)) {
            log.warn("地址不属于当前用户, 用户ID: {}, 地址ID: {}", userId, addressId);
            return false;
        }

        addressMapper.cancelDefaultByUserId(userId);
        address.setIsDefault(1);
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(address);

        clearAddressCache(userId);
        log.info("设置默认地址成功, 地址ID: {}", addressId);
        return true;
    }

    @Override
    public int countAddresses(Long userId) {
        return addressMapper.countByUserId(userId);
    }

    // ==================== 缓存私有方法 ====================

    private List<Address> getAddressListFromCache(Long userId) {
        String key = RedisConstants.ADDRESS_LIST_CACHE_KEY + userId;
        return redisUtils.getObject(key, new TypeReference<List<Address>>() {});
    }

    private void cacheAddressList(Long userId, List<Address> list) {
        String key = RedisConstants.ADDRESS_LIST_CACHE_KEY + userId;
        redisUtils.setObject(key, list, RedisConstants.ADDRESS_CACHE_TTL_HOURS, TimeUnit.HOURS);
    }

    private void clearAddressCache(Long userId) {
        String key = RedisConstants.ADDRESS_LIST_CACHE_KEY + userId;
        redisUtils.delete(key);
        log.debug("清除地址缓存, key: {}", key);
    }
}