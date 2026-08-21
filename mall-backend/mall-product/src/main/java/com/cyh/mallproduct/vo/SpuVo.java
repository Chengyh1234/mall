package com.cyh.mallproduct.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SPU VO（公开接口）
 * 用于 /spu/page、/spu/list、/spu/detail/{id}、/spu/by-store/{storeId}/page 等接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpuVo implements Serializable {

    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private String description;
    private String mainImage;
    private String images;
    private String unit;
    private String keywords;
    private Integer sales;
    private BigDecimal minPrice;
}