package com.cyh.malluser.vo;

import com.cyh.malluser.entity.Address;
import lombok.Data;

/**
 * 收货地址 VO
 */
@Data
public class AddressVo {

    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String zipCode;
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