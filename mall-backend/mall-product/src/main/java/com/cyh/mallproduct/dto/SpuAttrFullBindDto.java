package com.cyh.mallproduct.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * SPU完整属性绑定DTO
 * 一次性绑定SPU的基本属性和销售属性
 */
@Data
public class SpuAttrFullBindDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SPU ID
     */
    @NotNull(message = "SPU ID不能为空")
    private Long spuId;

    /**
     * 基本属性绑定列表
     */
    private List<SpuBasicAttrBindDto> basicAttrs;

    /**
     * 销售属性绑定列表
     */
    private List<SpuSaleAttrBindDto> saleAttrs;
}