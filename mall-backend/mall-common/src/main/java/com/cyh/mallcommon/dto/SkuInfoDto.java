package com.cyh.mallcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * SKU 信息 DTO（Feign 内部通信）
 * <p>
 * 用于 mall-order 通过 Feign 调用 mall-product 时传递 SKU 数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuInfoDto {

    /** SKU ID */
    private Long id;

    /** 所属 SPU ID */
    private Long spuId;

    /** 销售价格 */
    private BigDecimal price;

    /** 图片路径 */
    private String image;

    /** 库存数量 */
    private Integer stock;

    /** 冻结库存 */
    private Integer frozenStock;

    /** 状态（1-启用 0-禁用） */
    private Integer status;

    /** 重量(kg) */
    private BigDecimal weight;
}