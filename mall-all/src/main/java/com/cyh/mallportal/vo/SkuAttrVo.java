package com.cyh.mallportal.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * SKU属性视图对象
 * 包含SKU的销售属性完整信息
 */
@Data
public class SkuAttrVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * SPU名称
     */
    private String spuName;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * SKU图片
     */
    private String image;

    /**
     * 销售属性列表
     */
    private List<SkuSaleAttrVo> saleAttrs;

    /**
     * SKU销售属性VO
     */
    @Data
    public static class SkuSaleAttrVo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 记录ID
         */
        private Long id;

        /**
         * 属性ID
         */
        private Long attrId;

        /**
         * 属性名称
         */
        private String attrName;

        /**
         * 属性值ID
         */
        private Long attrValueId;

        /**
         * 属性值
         */
        private String attrValue;

        /**
         * 属性值图片URL
         */
        private String imageUrl;
    }
}
