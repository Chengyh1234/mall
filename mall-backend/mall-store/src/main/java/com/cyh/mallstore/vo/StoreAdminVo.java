package com.cyh.mallstore.vo;

import com.cyh.mallstore.entity.Store;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端店铺视图对象
 * 用于管理后台的店铺列表/详情展示，包含所有字段信息
 */
@Data
public class StoreAdminVo {

    private Long id;
    private String name;
    private Long sellerId;
    private String logo;
    private String banner;
    private String description;
    private String phone;
    private String address;
    private String businessLicense;
    private Integer status;
    private Integer sort;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StoreAdminVo fromStore(Store store) {
        if (store == null) {
            return null;
        }
        StoreAdminVo vo = new StoreAdminVo();
        vo.setId(store.getId());
        vo.setName(store.getName());
        vo.setSellerId(store.getSellerId());
        vo.setLogo(store.getLogo());
        vo.setBanner(store.getBanner());
        vo.setDescription(store.getDescription());
        vo.setPhone(store.getPhone());
        vo.setAddress(store.getAddress());
        vo.setBusinessLicense(store.getBusinessLicense());
        vo.setStatus(store.getStatus());
        vo.setSort(store.getSort());
        vo.setRejectReason(store.getRejectReason());
        vo.setCreatedAt(store.getCreatedAt());
        vo.setUpdatedAt(store.getUpdatedAt());
        return vo;
    }
}