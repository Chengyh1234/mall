package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.Store;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 店铺管理 VO
 *
 * 管理后台分页专用，包含店铺完整管理字段
 * 对应接口：
 *   GET /store/admin/page
 */
@Data
public class StoreAdminVo {

    /** 店铺ID */
    private Long id;

    /** 店铺名称 */
    private String name;

    /** 商家用户ID */
    private Long sellerId;

    /** 店铺Logo */
    private String logo;

    /** 店铺横幅 */
    private String banner;

    /** 店铺描述 */
    private String description;

    /** 联系电话 */
    private String phone;

    /** 店铺地址 */
    private String address;

    /** 营业执照 */
    private String businessLicense;

    /** 状态: 1-正常 0-禁用 2-审核中 3-审核失败 */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 审核驳回原因 */
    private String rejectReason;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
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