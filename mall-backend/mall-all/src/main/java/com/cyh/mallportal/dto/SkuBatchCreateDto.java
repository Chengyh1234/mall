package com.cyh.mallportal.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuBatchCreateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "SPU ID不能为空")
    private Long spuId;

    @NotNull(message = "销售价格不能为空")
    private BigDecimal price;

    @NotNull(message = "市场价不能为空")
    private BigDecimal marketPrice;

    @NotNull(message = "成本价不能为空")
    private BigDecimal costPrice;

    private Integer stock;

    private Integer warnStock;

    private String image;

    private BigDecimal weight;

    private Integer status;

    @NotEmpty(message = "销售属性值ID列表不能为空")
    private List<Long> attrValueIds;
}