package com.cyh.mallauth.feign;

import com.cyh.mallcommon.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 商品管理员统计 Feign 客户端
 * 调用 mall-product 获取平台商品统计数据
 */
@FeignClient(name = "mall-product", path = "/api/internal/product/admin")
public interface ProductAdminCountClient {

    /**
     * 获取平台商品总数（未删除的 SPU）
     */
    @GetMapping("/product-count")
    Result<Long> getProductCount();
}