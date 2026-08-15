package com.cyh.mallportal.vo;

import lombok.Data;

/**
 * SPU基本属性 VO（公开）
 *
 * 展示 SPU 已选的基本属性及值
 * 数据来源：spu_basic_attr_values → Attribute → AttributeValue
 * 对应接口：
 *   GET /spu/{spuId}/basic-attributes
 */
@Data
public class SpuBasicAttrVo {

    /** 属性ID */
    private Long attrId;

    /** 属性名称（如：屏幕尺寸、电池容量） */
    private String attrName;

    /** 属性值ID（预定义值时为 attribute_values.id，手动输入时为 null） */
    private Long valueId;

    /** 属性值内容（预定义值或手动输入值） */
    private String value;

    /** 属性值图片URL */
    private String imageUrl;
}