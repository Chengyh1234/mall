package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.dto.AddressDto;
import com.cyh.mallportal.entity.Address;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.service.AddressService;
import com.cyh.mallportal.vo.AddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收货地址管理控制器
 * 提供收货地址的增删改查功能
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /**
     * 添加收货地址
     *
     * @param addressDto 地址DTO
     * @return 添加结果
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public Result<Map<String, Object>> addAddress(@RequestBody AddressDto addressDto) {
        Long userId = getCurrentUserId();

        if (addressDto.getReceiverName() == null || addressDto.getReceiverName().trim().isEmpty()) {
            return Result.error("收货人不能为空");
        }
        if (addressDto.getReceiverPhone() == null || addressDto.getReceiverPhone().trim().isEmpty()) {
            return Result.error("收货人电话不能为空");
        }
        if (addressDto.getProvince() == null || addressDto.getProvince().trim().isEmpty()) {
            return Result.error("省不能为空");
        }
        if (addressDto.getCity() == null || addressDto.getCity().trim().isEmpty()) {
            return Result.error("市不能为空");
        }
        if (addressDto.getDistrict() == null || addressDto.getDistrict().trim().isEmpty()) {
            return Result.error("区不能为空");
        }
        if (addressDto.getDetailAddress() == null || addressDto.getDetailAddress().trim().isEmpty()) {
            return Result.error("详细地址不能为空");
        }

        Long addressId = addressService.addAddress(userId, addressDto);
        Map<String, Object> data = new HashMap<>();
        data.put("id", addressId);
        return Result.success("添加成功", data);
    }

    /**
     * 更新收货地址
     *
     * @param addressDto 地址DTO
     * @return 更新结果
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> updateAddress(@RequestBody AddressDto addressDto) {
        Long userId = getCurrentUserId();

        if (addressDto.getId() == null) {
            return Result.error("地址ID不能为空");
        }

        boolean success = addressService.updateAddress(userId, addressDto);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败，地址不存在或不属于当前用户");
    }

    /**
     * 删除收货地址
     *
     * @param addressId 地址ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{addressId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> deleteAddress(@PathVariable Long addressId) {
        Long userId = getCurrentUserId();

        boolean success = addressService.deleteAddress(userId, addressId);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败，地址不存在或不属于当前用户");
    }

    /**
     * 获取地址详情
     *
     * @param addressId 地址ID
     * @return 地址详情
     */
    @GetMapping("/detail/{addressId}")
    @PreAuthorize("hasRole('USER')")
    public Result<AddressVo> getAddressById(@PathVariable Long addressId) {
        Long userId = getCurrentUserId();

        Address address = addressService.getAddressById(userId, addressId);
        if (address != null) {
            return Result.success(AddressVo.fromAddress(address));
        }
        return Result.error("地址不存在或不属于当前用户");
    }

    /**
     * 获取当前用户的所有地址列表
     *
     * @return 地址列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public Result<List<AddressVo>> getAddressesByUserId() {
        Long userId = getCurrentUserId();

        List<Address> addresses = addressService.getAddressesByUserId(userId);
        List<AddressVo> voList = addresses.stream()
                .map(AddressVo::fromAddress)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取当前用户的默认地址
     *
     * @return 默认地址
     */
    @GetMapping("/default")
    @PreAuthorize("hasRole('USER')")
    public Result<AddressVo> getDefaultAddress() {
        Long userId = getCurrentUserId();

        Address address = addressService.getDefaultAddress(userId);
        if (address != null) {
            return Result.success(AddressVo.fromAddress(address));
        }
        return Result.error("默认地址不存在");
    }

    /**
     * 设置默认地址
     *
     * @param addressId 地址ID
     * @return 设置结果
     */
    @PutMapping("/set-default/{addressId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> setDefaultAddress(@PathVariable Long addressId) {
        Long userId = getCurrentUserId();

        boolean success = addressService.setDefaultAddress(userId, addressId);
        if (success) {
            return Result.success("设置成功", null);
        }
        return Result.error("设置失败，地址不存在或不属于当前用户");
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getId();
            }
        }
        return null;
    }
}
