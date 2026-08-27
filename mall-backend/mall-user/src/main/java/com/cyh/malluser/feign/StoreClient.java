package com.cyh.malluser.feign;

import com.cyh.mallcommon.dto.StoreInfoVo;
import com.cyh.mallcommon.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 店铺服务 Feign 客户端
 * <p>
 * 调用 mall-store 的内部接口，用于超级管理员将用户升级为商家时
 * 自动创建默认店铺。通过 Nacos 服务发现解析地址。
 */
@FeignClient(name = "mall-store", path = "/api")
public interface StoreClient {

    /**
     * 根据卖家 ID 查询店铺
     */
    @GetMapping("/internal/store/by-seller-id/{sellerId}")
    Result<StoreInfoVo> getBySellerId(@PathVariable("sellerId") Long sellerId);

    /**
     * 创建默认店铺（超级管理员升级商家时自动调用）
     * <p>
     * 只需要设置店铺名称和卖家 ID 即可，其他字段使用默认值。
     */
    @PostMapping("/internal/store/create-default")
    Result<Long> createDefault(@RequestBody CreateDefaultStoreRequest request);

    /**
     * 创建默认店铺请求 DTO
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class CreateDefaultStoreRequest {
        /**
         * 卖家 ID
         */
        private Long sellerId;
        /**
         * 店铺名称
         */
        private String storeName;
    }
}
