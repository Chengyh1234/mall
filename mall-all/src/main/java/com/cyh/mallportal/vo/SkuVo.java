package com.cyh.mallportal.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * SKU详情VO（公开接口，普通用户可见）
 * 仅包含用户购买决策所需的字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuVo {

    /** SKU ID */
    private Long id;

    /** 关联SPU ID */
    private Long spuId;

    /** 销售价格 */
    private BigDecimal price;

    /** 市场价（划线价） */
    private BigDecimal marketPrice;

    /** 图片路径 */
    private String image;

    /** 重量（kg） */
    private BigDecimal weight;

    /** 库存状态: true-有货 false-无货 */
    private Boolean inStock;

    /**
     * 销售属性列表
     * 每个属性包含：attrId, attrName, valueId, value, imageUrl
     */
    private List<Map<String, Object>> saleAttributes;
}