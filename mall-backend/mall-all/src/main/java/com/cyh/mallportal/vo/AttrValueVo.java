package com.cyh.mallportal.vo;

import lombok.Data;

/**
 * 属性值 VO（商户端）
 *
 * 某个属性下的可选属性值项，内嵌于 CategoryAttrVo.values 中使用
 */
@Data
public class AttrValueVo {

    /**
     * 属性值ID
     */
    private Long valueId;

    /**
     * 属性值名称（如：黑色、8G、5000mAh）
     */
    private String value;

    /**
     * 属性值图片URL（销售属性如图片色块使用）
     */
    private String imageUrl;

    /**
     * 排序（越小越靠前）
     */
    private Integer sort;
}