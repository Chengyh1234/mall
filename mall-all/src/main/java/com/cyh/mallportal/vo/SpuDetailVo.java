package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.Spu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品SPU详情VO
 * 包含商品基本信息及商家信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpuDetailVo implements Serializable {

    /**
     * 商品基本信息
     */
    private Spu spu;

    /**
     * 商家ID
     */
    private Long sellerId;

    /**
     * 商家用户名
     */
    private String sellerUsername;

    /**
     * 商家头像
     */
    private String sellerAvatar;

    /**
     * 商家真实姓名
     */
    private String sellerRealName;

    /**
     * 商家手机号
     */
    private String sellerPhone;
}
