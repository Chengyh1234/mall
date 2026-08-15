package com.cyh.mallportal.vo;

import com.cyh.mallportal.vo.AttrValueVo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 属性视图对象（含属性值列表）
 *
 * 用于管理后台展示属性详情，包含该属性下的所有属性值
 */
@Data
public class AttributeVo {

    /**
     * 属性ID
     */
    private Long id;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 属性类型：1=销售属性，2=基本属性
     */
    private Integer attrType;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 该属性下的属性值列表
     */
    private List<AttrValueVo> values;
}