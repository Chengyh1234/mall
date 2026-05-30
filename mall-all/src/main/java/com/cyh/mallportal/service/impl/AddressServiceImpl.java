package com.cyh.mallportal.service.impl;

import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallportal.dto.AddressDto;
import com.cyh.mallportal.entity.Address;
import com.cyh.mallportal.mapper.AddressMapper;
import com.cyh.mallportal.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收货地址服务实现类
 * 提供收货地址业务逻辑的具体实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    /**
     * 最大地址数量限制
     */
    private static final int MAX_ADDRESS_COUNT = 10;

    /**
     * 添加收货地址
     *
     * @param userId    用户ID
     * @param addressDto 地址DTO
     * @return 地址ID
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
        log.info("添加收货地址成功, 地址ID: {}", address.getId());
        return address.getId();
    }

    /**
     * 更新收货地址
     *
     * @param userId    用户ID
     * @param addressDto 地址DTO
     * @return 是否更新成功
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
            log.info("更新收货地址成功, 地址ID: {}", addressDto.getId());
        } else {
            log.warn("更新收货地址失败, 地址ID: {}", addressDto.getId());
        }

        return success;
    }

    /**
     * 删除收货地址
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 是否删除成功
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
        log.info("删除收货地址成功, 地址ID: {}", addressId);
        return true;
    }

    /**
     * 根据ID获取地址详情
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 地址实体
     */
    @Override
    public Address getAddressById(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address != null && address.getUserId().equals(userId)) {
            return address;
        }
        return null;
    }

    /**
     * 获取用户所有地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        return addressMapper.selectByUserId(userId);
    }

    /**
     * 获取用户默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    @Override
    public Address getDefaultAddress(Long userId) {
        return addressMapper.selectDefaultByUserId(userId);
    }

    /**
     * 设置默认地址
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 是否设置成功
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

        log.info("设置默认地址成功, 地址ID: {}", addressId);
        return true;
    }

    /**
     * 获取用户地址数量
     *
     * @param userId 用户ID
     * @return 地址数量
     */
    @Override
    public int countAddresses(Long userId) {
        return addressMapper.countByUserId(userId);
    }
}