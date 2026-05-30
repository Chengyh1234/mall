package com.cyh.mallportal.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * SPU基本属性绑定DTO
 * 用于商家绑定/更新SPU的基本属性值
 */
@Data

public class SpuBasicAttrBindDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID（更新时必填，绑定时不填）
     */
    private Long id;

    /**
     * SPU ID
     */
    @NotNull(message = "SPU ID不能为空")
    private Long spuId;

    /**
     * 基本属性ID
     */
    @NotNull(message = "属性ID不能为空")
    private Long attrId;

    /**
     * 属性值ID（当属性为单选/多选时使用）
     * 与manualValue二选一
     */
    private Long attrValueId;

    /**
     * 手动输入的属性值（当属性为手动输入时使用）
     * 与attrValueId二选一
     */
    private String manualValue;
}
