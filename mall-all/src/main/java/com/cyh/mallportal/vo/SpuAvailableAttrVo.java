package com.cyh.mallportal.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * SPU可用属性VO
 * 显示分类下的所有属性，标记哪些已绑定
 */
@Data
public class SpuAvailableAttrVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 基本属性列表
     */
    private List<AvailableAttrItem> basicAttrs;

    /**
     * 销售属性列表
     */
    private List<AvailableAttrItem> saleAttrs;

    /**
     * 可用属性项
     */
    @Data
    public static class AvailableAttrItem implements Serializable {

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
         * 属性类型
         */
        private Integer attrType;

        /**
         * 是否已绑定
         */
        private Boolean bound;

        /**
         * 绑定记录ID（如果已绑定）
         */
        private Long boundId;

        /**
         * 可选属性值列表
         */
        private List<Map<String, Object>> values;

        /**
         * 当前绑定的值列表（如果已绑定）
         * 基本属性：列表含 0 或 1 个元素（单个绑定值）
         * 销售属性：列表含 0 到 N 个元素（可绑定多个值）
         */
        private List<Map<String, Object>> currentValues;
    }
}
