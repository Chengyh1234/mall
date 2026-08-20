package com.cyh.malluser.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import com.cyh.malluser.dto.AddressDto;
import com.cyh.malluser.entity.Address;
import com.cyh.malluser.service.AddressService;
import com.cyh.malluser.vo.AddressVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收货地址管理控制器
 * <p>
 * 从网关注入的请求头 X-User-Id 获取当前用户ID。
 * 角色校验由 {@link PreAuthorize} 注解处理，SecurityContext 由 {@link com.cyh.malluser.filter.GatewayHeaderAuthenticationFilter} 构建。
 */
@Slf4j
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class AddressController {

    private final AddressService addressService;

    /**
     * 添加收货地址
     */
    @PostMapping("/add")
    public Result<Map<String, Object>> addAddress(@RequestHeader("X-User-Id") Long userId,
                                                  @RequestBody @Validated(Create.class) AddressDto addressDto) {
        Long addressId = addressService.addAddress(userId, addressDto);
        Map<String, Object> data = new HashMap<>();
        data.put("id", addressId);
        return Result.success("添加成功", data);
    }

    /**
     * 更新收货地址
     */
    @PutMapping("/update")
    public Result<Void> updateAddress(@RequestHeader("X-User-Id") Long userId,
                                      @RequestBody @Validated(Update.class) AddressDto addressDto) {
        boolean success = addressService.updateAddress(userId, addressDto);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败，地址不存在或不属于当前用户");
    }

    /**
     * 删除收货地址
     */
    @DeleteMapping("/delete/{addressId}")
    public Result<Void> deleteAddress(@RequestHeader("X-User-Id") Long userId,
                                      @PathVariable Long addressId) {
        boolean success = addressService.deleteAddress(userId, addressId);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败，地址不存在或不属于当前用户");
    }

    /**
     * 获取地址详情
     */
    @GetMapping("/detail/{addressId}")
    public Result<AddressVo> getAddressById(@RequestHeader("X-User-Id") Long userId,
                                            @PathVariable Long addressId) {
        Address address = addressService.getAddressById(userId, addressId);
        if (address != null) {
            return Result.success(AddressVo.fromAddress(address));
        }
        return Result.error("地址不存在或不属于当前用户");
    }

    /**
     * 获取当前用户的所有地址列表
     */
    @GetMapping("/list")
    public Result<List<AddressVo>> getAddressesByUserId(@RequestHeader("X-User-Id") Long userId) {
        List<Address> addresses = addressService.getAddressesByUserId(userId);
        List<AddressVo> voList = addresses.stream()
                .map(AddressVo::fromAddress)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取当前用户的默认地址
     */
    @GetMapping("/default")
    public Result<AddressVo> getDefaultAddress(@RequestHeader("X-User-Id") Long userId) {
        Address address = addressService.getDefaultAddress(userId);
        if (address != null) {
            return Result.success(AddressVo.fromAddress(address));
        }
        return Result.error("默认地址不存在");
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/set-default/{addressId}")
    public Result<Void> setDefaultAddress(@RequestHeader("X-User-Id") Long userId,
                                          @PathVariable Long addressId) {
        boolean success = addressService.setDefaultAddress(userId, addressId);
        if (success) {
            return Result.success("设置成功", null);
        }
        return Result.error("设置失败，地址不存在或不属于当前用户");
    }
}