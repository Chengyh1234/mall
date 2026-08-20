package com.cyh.malluser.service;

import com.cyh.malluser.dto.AddressDto;
import com.cyh.malluser.entity.Address;

import java.util.List;

/**
 * 收货地址服务接口
 */
public interface AddressService {

    /**
     * 添加收货地址
     */
    Long addAddress(Long userId, AddressDto addressDto);

    /**
     * 更新收货地址
     */
    boolean updateAddress(Long userId, AddressDto addressDto);

    /**
     * 删除收货地址
     */
    boolean deleteAddress(Long userId, Long addressId);

    /**
     * 根据ID获取地址详情
     */
    Address getAddressById(Long userId, Long addressId);

    /**
     * 获取用户所有地址列表
     */
    List<Address> getAddressesByUserId(Long userId);

    /**
     * 获取用户默认地址
     */
    Address getDefaultAddress(Long userId);

    /**
     * 设置默认地址
     */
    boolean setDefaultAddress(Long userId, Long addressId);

    /**
     * 获取用户地址数量
     */
    int countAddresses(Long userId);
}