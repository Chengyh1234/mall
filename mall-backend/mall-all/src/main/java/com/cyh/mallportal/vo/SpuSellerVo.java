package com.cyh.mallportal.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家端商品SPU分页VO
 * 用于 /spu/page-by-seller/{sellerId} 接口返回给前端
 * 继承 SpuVo，追加商家管理所需的字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SpuSellerVo extends SpuVo implements Serializable {

    private Long sellerId;
    private Long storeId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}