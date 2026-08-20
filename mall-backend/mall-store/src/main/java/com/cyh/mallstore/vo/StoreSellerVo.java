package com.cyh.mallstore.vo;

import com.cyh.mallstore.entity.Store;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家店铺视图对象
 * 用于商家端查看自己的店铺信息，包含完整信息及审核状态
 */
@Data
public class StoreSellerVo {

    private Long id;
    private String name;
    private String logo;
    private String banner;
    private String description;
    private String phone;
    private String address;
    private String businessLicense;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createdAt;

    public static StoreSellerVo fromStore(Store store) {
        if (store == null) {
            return null;
        }
        StoreSellerVo vo = new StoreSellerVo();
        vo.setId(store.getId());
        vo.setName(store.getName());
        vo.setLogo(store.getLogo());
        vo.setBanner(store.getBanner());
        vo.setDescription(store.getDescription());
        vo.setPhone(store.getPhone());
        vo.setAddress(store.getAddress());
        vo.setBusinessLicense(store.getBusinessLicense());
        vo.setStatus(store.getStatus());
        vo.setRejectReason(store.getRejectReason());
        vo.setCreatedAt(store.getCreatedAt());
        return vo;
    }
}