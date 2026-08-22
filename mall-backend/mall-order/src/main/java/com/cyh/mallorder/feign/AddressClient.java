package com.cyh.mallorder.feign;

import com.cyh.mallcommon.dto.AddressDto;
import com.cyh.mallcommon.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户地址服务 Feign 客户端
 * <p>
 * 调用 mall-user 的内部接口获取收货地址数据，替代直连数据库。
 */
@FeignClient(name = "mall-user", path = "/api")
public interface AddressClient {

    /**
     * 根据地址 ID 获取收货地址详情
     */
    @GetMapping("/internal/address/detail/{id}")
    Result<AddressDto> getAddressDetail(@PathVariable("id") Long id);
}