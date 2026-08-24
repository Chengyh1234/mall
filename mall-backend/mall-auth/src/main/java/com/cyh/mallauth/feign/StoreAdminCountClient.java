package com.cyh.mallauth.feign;

import com.cyh.mallcommon.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 店铺管理员统计 Feign 客户端
 * 调用 mall-store 获取平台店铺统计数据
 */
@FeignClient(name = "mall-store", path = "/api/internal/store/admin")
public interface StoreAdminCountClient {

    /**
     * 获取已入驻卖家数
     */
    @GetMapping("/settled-seller-count")
    Result<Long> getSettledSellerCount();
}