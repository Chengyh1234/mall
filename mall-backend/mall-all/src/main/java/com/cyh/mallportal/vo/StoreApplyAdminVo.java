package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.Store;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开店申请管理 VO
 *
 * 管理员审核专用，包含审核所需的店铺申请信息
 * 对应接口：
 *   GET /admin/store/apply/pending
 */
@Data
public class StoreApplyAdminVo {

    /** 店铺ID */
    private Long id;

    /** 店铺名称 */
    private String name;

    /** 商家用户ID */
    private Long sellerId;

    /** 店铺描述 */
    private String description;

    /** 联系电话 */
    private String phone;

    /** 店铺地址 */
    private String address;

    /** 状态: 1-正常 0-禁用 2-审核中 3-审核失败 */
    private Integer status;

    /** 审核驳回原因 */
    private String rejectReason;

    /** 创建时间 */
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