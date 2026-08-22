package com.cyh.mallcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SKU 库存 DTO（Feign 内部通信）
 * <p>
 * 轻量级，仅包含库存相关字段，用于库存初始化场景
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuStockDto {

    /** SKU ID */
    private Long id;

    /** 总库存 */
    private Integer stock;

    /** 冻结库存 */
    private Integer frozenStock;
}