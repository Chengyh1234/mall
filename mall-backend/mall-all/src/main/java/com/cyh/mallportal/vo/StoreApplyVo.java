//package com.cyh.mallportal.vo;
//
//import com.cyh.mallportal.entity.Store;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//
///**
// * 开店申请 VO
// *
// * 用户端申请开店/查看审核状态专用，排除 sellerId/logo/banner/businessLicense/sort 等字段
// * 对应接口：
// *   POST /store/apply
// *   GET /store/apply/status
// */
//@Data
//public class StoreApplyVo {
//
//    /** 店铺ID */
//    private Long id;
//
//    /** 店铺名称 */
//    private String name;
//
//    /** 店铺描述 */
//    private String description;
//
//    /** 联系电话 */
//    private String phone;
//
//    /** 店铺地址 */
//    private String address;
//
//    /** 状态: 1-正常 0-禁用 2-审核中 3-审核失败 */
//    private Integer status;
//
//    /** 审核驳回原因 */
//    private String rejectReason;
//
//    /** 创建时间 */
//    private LocalDateTime createdAt;
//
//    public static StoreApplyVo fromStore(Store store) {
//        if (store == null) {
//            return null;
//        }
//        StoreApplyVo vo = new StoreApplyVo();
//        vo.setId(store.getId());
//        vo.setName(store.getName());
//        vo.setDescription(store.getDescription());
//        vo.setPhone(store.getPhone());
//        vo.setAddress(store.getAddress());
//        vo.setStatus(store.getStatus());
//        vo.setRejectReason(store.getRejectReason());
//        vo.setCreatedAt(store.getCreatedAt());
//        return vo;
//    }
//}