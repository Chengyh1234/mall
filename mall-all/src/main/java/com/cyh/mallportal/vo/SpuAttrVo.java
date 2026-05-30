package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.SpuBasicAttrValue;
import com.cyh.mallportal.entity.SpuSaleAttrChoice;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * SPU属性视图对象
 * 包含SPU的基本属性和销售属性完整信息
 */
@Data
public class SpuAttrVo implements Serializable {

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
     * 基本属性列表
     */
    private List<SpuBasicAttrDetailVo> basicAttrs;

    /**
     * 销售属性列表
     */
    private List<SpuSaleAttrDetailVo> saleAttrs;

    /**
     * SPU基本属性详情VO
     */
    @Data
    public static class SpuBasicAttrDetailVo implements Serializable {
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
         * 属性类型：1=销售属性，2=基本属性
         */
        private Integer attrType;

        /**
         * 属性值ID
         */
        private Long attrValueId;

        /**
         * 属性值
         */
        private String attrValue;

        /**
         * 手动输入值
         */
        private String manualValue;

        /**
         * 属性值图片URL
         */
        private String imageUrl;
    }

    /**
     * SPU销售属性详情VO
     */
    @Data
    public static class SpuSaleAttrDetailVo implements Serializable {
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
         * 可选属性值列表
         */
        private List<AttrValueVo> selectedValues;
    }

    /**
     * 属性值VO
     */
    @Data
    public static class AttrValueVo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 属性值ID
         */
        private Long valueId;

        /**
         * 属性值
         */
        private String value;

        /**
         * 属性值图片URL
         */
        private String imageUrl;
    }
}
