package com.cyh.mallportal.service;

import com.cyh.mallportal.dto.AddressDto;
import com.cyh.mallportal.entity.Address;

import java.util.List;

/**
 * 收货地址服务接口
 * 提供收货地址业务逻辑操作
 */
public interface AddressService {

    /**
     * 添加收货地址
     *
     * @param userId    用户ID
     * @param addressDto 地址DTO
     * @return 地址ID
     */
    Long addAddress(Long userId, AddressDto addressDto);

    /**
     * 更新收货地址
     *
     * @param userId    用户ID
     * @param addressDto 地址DTO
     * @return 是否更新成功
     */
    boolean updateAddress(Long userId, AddressDto addressDto);

    /**
     * 删除收货地址
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 是否删除成功
     */
    boolean deleteAddress(Long userId, Long addressId);

    /**
     * 根据ID获取地址详情
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 地址实体
     */
    Address getAddressById(Long userId, Long addressId);

    /**
     * 获取用户所有地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> getAddressesByUserId(Long userId);

    /**
     * 获取用户默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    Address getDefaultAddress(Long userId);

    /**
     * 设置默认地址
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 是否设置成功
     */
    boolean setDefaultAddress(Long userId, Long addressId);

    /**
     * 获取用户地址数量
     *
     * @param userId 用户ID
     * @return 地址数量
     */
    int countAddresses(Long userId);
}