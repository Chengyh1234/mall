package com.cyh.mallportal.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * SPU销售属性绑定DTO
 * 用于商家绑定/更新SPU的销售属性及可选值
 */
@Data
public class SpuSaleAttrBindDto implements Serializable {

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
     * 销售属性ID
     */
    @NotNull(message = "属性ID不能为空")
    private Long attrId;

    /**
     * 该SPU启用的属性值ID列表
     * 例如：[1, 2, 3, 4] 表示该SPU有4个颜色可选
     */
    @NotNull(message = "属性值ID列表不能为空")
    private List<Long> selectedValueIds;
}
