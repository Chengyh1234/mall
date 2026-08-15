package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.Address;
import lombok.Data;

/**
 * 收货地址 VO
 *
 * 用户端收货地址展示专用，排除 userId、创建/更新时间等敏感和冗余字段
 * 对应接口：
 *   GET /address/detail/{addressId}
 *   GET /address/list
 *   GET /address/default
 */
@Data
public class AddressVo {

    /** 主键ID */
    private Long id;

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

    /** 邮编 */
    private String zipCode;

    /** 是否默认：1-是，0-否 */
    private Integer isDefault;

    public static AddressVo fromAddress(Address address) {
        AddressVo vo = new AddressVo();
        vo.setId(address.getId());
        vo.setReceiverName(address.getReceiverName());
        vo.setReceiverPhone(address.getReceiverPhone());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setDistrict(address.getDistrict());
        vo.setDetailAddress(address.getDetailAddress());
        vo.setZipCode(address.getZipCode());
        vo.setIsDefault(address.getIsDefault());
        return vo;
    }
}