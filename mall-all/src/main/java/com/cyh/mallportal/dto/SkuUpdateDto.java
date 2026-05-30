package com.cyh.mallportal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuUpdateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "SKU ID不能为空")
    private Long id;

    private BigDecimal price;

    private BigDecimal marketPrice;

    private BigDecimal costPrice;

    private Integer stock;

    private Integer warnStock;

    private String image;

    private BigDecimal weight;

    private Integer status;

    private List<Long> attrValueIds;
}