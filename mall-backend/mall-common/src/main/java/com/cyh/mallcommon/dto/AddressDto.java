package com.cyh.mallcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收货地址 DTO（Feign 内部通信）
 * <p>
 * 用于 mall-order 通过 Feign 调用 mall-user 时传递地址数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 收货人 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 省 */
    private String province;

    /** 市 */
    private String city;

    /** 区 */
    private String district;

    /** 详细地址 */
    private String detailAddress;
}