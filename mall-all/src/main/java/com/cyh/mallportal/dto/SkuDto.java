package com.cyh.mallportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * SKU新增/修改DTO
 * 参数说明：
 * - 基本信息：id（修改时必填）, spuId, skuCode, price, marketPrice, costPrice
 * - 库存相关：stock, warnStock
 * - 商品信息：specs(JSON格式), weight
 * - 图片：imageFile(上传文件) 或 image(已有路径)
 * - 状态：status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuDto {

    private Long id;

    private Long spuId;

    private String skuCode;

    private BigDecimal price;

    private BigDecimal marketPrice;

    private BigDecimal costPrice;

    private Integer stock;

    private Integer warnStock;

    private String image;

    private String specs;

    private BigDecimal weight;

    private Integer status;

    private MultipartFile imageFile;
}
