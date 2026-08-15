package com.cyh.mallportal.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * SPU属性视图对象
 * 包含SPU的基本属性和销售属性完整信息，以及选过不是该分类下全部属性
 * {"spuId":54,"spuName":"华硕天选5pro",
 * "basicAttrs":
 *   {"id":47,"attrId":6,"attrName":"屏幕尺寸","attrType":2,"attrValueId":11,"attrValue":"6.73英寸","manualValue":null,"imageUrl":null}],
 *  "saleAttrs":
 *  [{"id":34,"attrId":1,"attrName":"颜色","selectedValues":[{"valueId":20,"value":"电脑黑色","imageUrl":null}]
 *  }]
 *  }
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

        /**
         * 是否卖家自定义值（true=卖家自定义，false=平台预设）
         */
        private Boolean custom;
    }
}
