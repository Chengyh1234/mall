package com.cyh.mallproduct.feign;

import com.cyh.mallcommon.dto.StoreInfoVo;
import com.cyh.mallcommon.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 店铺服务 Feign 客户端
 * <p>
 * 调用 mall-store 的内部接口获取店铺数据，替代直连数据库。
 * 服务名 mall-store 通过 Nacos 服务发现解析。
 */
@FeignClient(name = "mall-store", path = "/api")
public interface StoreClient {

    /**
     * 根据店铺 ID 获取店铺基本信息
     */
    @GetMapping("/internal/store/detail/{id}")
    Result<StoreInfoVo> getStoreDetail(@PathVariable("id") Long id);

    /**
     * 根据当前商家获取其店铺信息
     */
    @GetMapping("/internal/store/by-seller")
    Result<StoreInfoVo> getStoreBySeller();
}