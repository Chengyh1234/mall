package com.cyh.mallportal.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SKU详情VO（管理员端可见）
 * 继承 SkuStoreVo，追加管理员监管所需的字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SkuAdminVo extends SkuStoreVo implements Serializable {

    /** 冻结库存（预扣，待支付） */
    private Integer frozenStock;

    /** 逻辑删除（0-未删除 1-已删除） */
    private Boolean isDeleted;
}