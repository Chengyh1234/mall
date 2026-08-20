package com.cyh.mallstore.vo;

import com.cyh.mallstore.entity.Store;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开店申请管理端视图对象
 * 用于管理后台查看开店申请列表，包含申请信息及审核状态
 */
@Data
public class StoreApplyAdminVo {

    private Long id;
    private String name;
    private Long sellerId;
    private String description;
    private String phone;
    private String address;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createdAt;

    public static StoreApplyAdminVo fromStore(Store store) {
        if (store == null) {
            return null;
        }
        StoreApplyAdminVo vo = new StoreApplyAdminVo();
        vo.setId(store.getId());
        vo.setName(store.getName());
        vo.setSellerId(store.getSellerId());
        vo.setDescription(store.getDescription());
        vo.setPhone(store.getPhone());
        vo.setAddress(store.getAddress());
        vo.setStatus(store.getStatus());
        vo.setRejectReason(store.getRejectReason());
        vo.setCreatedAt(store.getCreatedAt());
        return vo;
    }
}