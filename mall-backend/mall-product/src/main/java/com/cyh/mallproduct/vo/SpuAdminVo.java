package com.cyh.mallproduct.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 运营管理员端商品SPU分页VO
 * 用于 /spu/page-all 接口返回给前端
 * 继承 SpuSellerVo，追加管理员所需的字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SpuAdminVo extends SpuSellerVo implements Serializable {

    private Boolean isDeleted;
}