package com.cyh.mallproduct.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 运营管理员端商品管理详情VO
 * 用于 /spu/manage-detail/admin/{id} 接口返回给管理员
 * 继承 SpuAdminVo，追加商家用户信息字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SpuAdminDetailVo extends SpuAdminVo implements Serializable {

    /** 商家用户名 */
    private String sellerUsername;

    /** 商家头像 */
    private String sellerAvatar;

    /** 商家真实姓名 */
    private String sellerRealName;

    /** 商家手机号 */
    private String sellerPhone;
}