package com.cyh.mallportal.dto;


import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * SKU销售属性绑定DTO
 * 用于商家绑定SKU的销售属性值
 */
@Data
public class SkuSaleAttrBindDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    /**
     * 销售属性值ID列表
     * 例如：[1, 5, 7] 表示该SKU是黑色、12G内存、256GB存储
     */
    @NotNull(message = "属性值ID列表不能为空")
    private List<Long> attrValueIds;
}
