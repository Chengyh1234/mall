package com.cyh.mallproduct.controller;

import com.cyh.mallcommon.dto.SkuInfoDto;
import com.cyh.mallcommon.dto.SkuStockDto;
import com.cyh.mallcommon.dto.SpuInfoDto;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallproduct.entity.Sku;
import com.cyh.mallproduct.entity.Spu;
import com.cyh.mallproduct.service.SkuService;
import com.cyh.mallproduct.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SKU/SPU 内部接口控制器（供其他微服务 Feign 调用）
 * <p>
 * 路径统一使用 /internal/ 前缀，区别于对外公开接口。
 * 返回纯数据 DTO，不含脱敏、缓存等对外逻辑。
 */
@RestController
@RequestMapping("/internal/sku")
@RequiredArgsConstructor
public class SkuInternalController {

    private final SkuService skuService;

    /**
     * 根据 SKU ID 获取 SKU 基本信息
     */
    @GetMapping("/detail/{id}")
    public Result<SkuInfoDto> getSkuDetail(@PathVariable Long id) {
        Sku sku = skuService.getById(id);
        if (sku == null) {
            return Result.error("SKU不存在");
        }
        SkuInfoDto dto = new SkuInfoDto(
                sku.getId(), sku.getSpuId(), sku.getPrice(),
                sku.getImage(), sku.getStock(), sku.getFrozenStock(),
                sku.getStatus(), sku.getWeight()
        );
        return Result.success(dto);
    }

    /**
     * 同步库存到 MySQL
     * 将 Redis 中的库存数据同步回 MySQL 的 sku 表
     */
    @PutMapping("/sync-stock")
    public Result<Void> syncStock(@RequestParam Long skuId,
                                  @RequestParam Integer stock,
                                  @RequestParam Integer frozenStock) {
        Sku sku = skuService.getById(skuId);
        if (sku == null) {
            return Result.error("SKU不存在");
        }
        sku.setStock(stock);
        sku.setFrozenStock(frozenStock);
        skuService.update(sku);
        return Result.success(null);
    }

    /**
     * 获取所有 SKU 的库存信息（用于应用启动时加载到 Redis）
     */
    @GetMapping("/all-stock")
    public Result<List<SkuStockDto>> getAllSkuStock() {
        List<Sku> skuList = skuService.getAll();
        List<SkuStockDto> dtos = skuList.stream()
                .map(sku -> new SkuStockDto(
                        sku.getId(),
                        sku.getStock(),
                        sku.getFrozenStock()
                ))
                .collect(Collectors.toList());
        return Result.success(dtos);
    }
}