package com.cyh.mallportal.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * SKU可用属性VO
 * 显示SPU选择的销售属性及可选值，供SKU创建时选择
 */
@Data
public class SkuAvailableAttrVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * SPU名称
     */
    private String spuName;

    /**
     * 销售属性列表（SPU已选择的）
     */
    private List<SkuAvailableAttrItem> saleAttrs;

    /**
     * SKU可用属性项
     */
    @Data
    public static class SkuAvailableAttrItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 属性ID
         */
        private Long attrId;

        /**
         * 属性名称
         */
        private String attrName;

        /**
         * SPU选择的属性值列表
         */
        private List<Map<String, Object>> availableValues;

        /**
         * 是否必填（销售属性通常都是必填）
         */
        private Boolean required;
    }
}
