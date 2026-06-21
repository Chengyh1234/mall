package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.Store;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 店铺详情 VO
 * 用于公开查询店铺详情，包含店铺完整展示信息，不含内部管理字段
 */
@Data
public class StoreDetailVo {

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

    /** 店铺地址 */
    private String address;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /**
     * 从 Store 实体创建 StoreDetailVo
     */
    public static StoreDetailVo fromStore(Store store) {
        if (store == null) {
            return null;
        }
        StoreDetailVo vo = new StoreDetailVo();
        vo.setId(store.getId());
        vo.setName(store.getName());
        vo.setLogo(store.getLogo());
        vo.setBanner(store.getBanner());
        vo.setDescription(store.getDescription());
        vo.setAddress(store.getAddress());
        vo.setCreatedAt(store.getCreatedAt());
        return vo;
    }
}