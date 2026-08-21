package com.cyh.mallproduct.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SKU详情VO（商家端可见）
 * 继承 SkuVo，追加商家经营管理所需的字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SkuStoreVo extends SkuVo implements Serializable {

    /** 成本价 */
    private BigDecimal costPrice;

    /** 库存数量 */
    private Integer stock;

    /** 预警库存 */
    private Integer warnStock;

    /** 状态（1-启用 0-禁用） */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}