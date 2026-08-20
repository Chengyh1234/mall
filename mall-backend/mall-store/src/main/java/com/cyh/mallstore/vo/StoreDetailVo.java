package com.cyh.mallstore.vo;

import com.cyh.mallstore.entity.Store;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 店铺详情视图对象
 * 用于前端店铺详情页展示，包含店铺基本信息、地址、创建时间等
 */
@Data
public class StoreDetailVo {

    private Long id;
    private String name;
    private String logo;
    private String banner;
    private String description;
    private String address;
    private LocalDateTime createdAt;

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