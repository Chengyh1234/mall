package com.cyh.malluser.controller;

import com.cyh.mallcommon.dto.AddressDto;
import com.cyh.mallcommon.utils.Result;
import com.cyh.malluser.entity.Address;
import com.cyh.malluser.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 地址内部接口控制器（供其他微服务 Feign 调用）
 * <p>
 * 路径统一使用 /internal/address/ 前缀，区别于对外公开接口。
 */
@RestController
@RequestMapping("/internal/address")
@RequiredArgsConstructor
public class AddressInternalController {

    private final AddressService addressService;

    /**
     * 根据地址 ID 获取收货地址详情
     */
    @GetMapping("/detail/{id}")
    public Result<AddressDto> getAddressDetail(@PathVariable Long id) {
        Address address = addressService.getAddressById(id);
        if (address == null) {
            return Result.error("地址不存在");
        }
        AddressDto dto = new AddressDto(
                address.getId(), address.getUserId(),
                address.getReceiverName(), address.getReceiverPhone(),
                address.getProvince(), address.getCity(),
                address.getDistrict(), address.getDetailAddress()
        );
        return Result.success(dto);
    }
}