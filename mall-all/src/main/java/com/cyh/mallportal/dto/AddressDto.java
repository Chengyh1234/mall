package com.cyh.mallportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收货地址DTO
 * 参数说明：
 * - 基本信息：id（修改时必填）
 * - 收货人信息：receiverName, receiverPhone
 * - 地址信息：province, city, district, detailAddress, zipCode
 * - 默认地址：isDefault
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    /**
     * 地址ID（修改时必填）
     */
    private Long id;

    /**
     * 收货人
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 邮编
     */
    private String zipCode;

    /**
     * 是否默认: 1-是 0-否
     */
    private Integer isDefault;
}