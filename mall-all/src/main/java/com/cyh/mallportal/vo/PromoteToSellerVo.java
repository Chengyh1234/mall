package com.cyh.mallportal.vo;

import lombok.Data;

/**
 * 升级商家结果 VO
 *
 * 对应接口：
 *   PUT /user/manage/promote-to-seller/{userId}
 */
@Data
public class PromoteToSellerVo {

    /** 用户ID */
    private Long userId;

    /** 商家角色ID */
    private Long roleId;

    /** 商家角色编码 */
    private String roleCode;

    /** 商家角色名称 */
    private String roleName;

    /** 店铺ID */
    private Long storeId;
}