package com.cyh.mallportal.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * SKU详情VO（包含销售属性）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuVo {

    /**
     * SKU ID
     */
    private Long id;

    /**
     * 关联SPU ID
     */
    private Long spuId;

    /**
     * SKU编码
     */
    //private String skuCode;

    /**
     * 销售价格
     */
    private BigDecimal price;

    /**
     * 市场价
     */
    private BigDecimal marketPrice;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 预警库存
     */
    private Integer warnStock;

    /**
     * 图片路径
     */
    private String image;

    /**
     * 重量（kg）
     */
    private BigDecimal weight;

    /**
     * 状态（1-启用 0-禁用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 销售属性列表
     * 每个属性包含：attrId, attrName, valueId, value, imageUrl
     */
    private List<Map<String, Object>> saleAttributes;
}
