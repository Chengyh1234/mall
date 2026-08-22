package com.cyh.mallorder.feign;

import com.cyh.mallcommon.dto.SkuInfoDto;
import com.cyh.mallcommon.dto.SkuStockDto;
import com.cyh.mallcommon.dto.SpuInfoDto;
import com.cyh.mallcommon.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 商品服务 Feign 客户端
 * <p>
 * 调用 mall-product 的内部接口获取 SKU/SPU 数据，替代直连数据库。
 */
@FeignClient(name = "mall-product", path = "/api")
public interface SkuClient {

    /**
     * 根据 SKU ID 获取 SKU 基本信息
     */
    @GetMapping("/internal/sku/detail/{id}")
    Result<SkuInfoDto> getSkuDetail(@PathVariable("id") Long id);

    /**
     * 根据 SPU ID 获取 SPU 基本信息
     */
    @GetMapping("/internal/spu/detail/{id}")
    Result<SpuInfoDto> getSpuDetail(@PathVariable("id") Long id);

    /**
     * 同步库存到 MySQL
     * 将 Redis 中的库存数据同步回 MySQL 的 sku 表
     */
    @PutMapping("/internal/sku/sync-stock")
    Result<Void> syncStock(@RequestParam("skuId") Long skuId,
                           @RequestParam("stock") Integer stock,
                           @RequestParam("frozenStock") Integer frozenStock);

    /**
     * 获取所有 SKU 的库存信息（用于应用启动时加载到 Redis）
     */
    @GetMapping("/internal/sku/all-stock")
    Result<List<SkuStockDto>> getAllSkuStock();
}