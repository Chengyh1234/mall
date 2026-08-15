package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.Store;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 卖家店铺 VO
 *
 * 卖家查看自己店铺专用，排除 sort(排序号)/updatedAt 等内部管理字段
 * 对应接口：
 *   GET /store/my-store
 */
@Data
public class StoreSellerVo {

    /** 店铺ID */
    private Long id;

    /** 店铺名称 */
    private String name;

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

    /** 审核驳回原因 */
    private String rejectReason;

    /** 创建时间 */
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