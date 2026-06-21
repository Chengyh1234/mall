package com.cyh.mallportal.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品SPU详情VO（公开接口）
 * 用于 /spu/detail/{id} 接口返回给前端
 * 包含 SpuVo 字段 + 商家基本信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SpuDetailVo extends SpuVo implements Serializable {

    /** 商家ID */
    private Long sellerId;

    /** 商家用户名 */
    private String sellerUsername;

    /** 商家头像 */
    private String sellerAvatar;
}