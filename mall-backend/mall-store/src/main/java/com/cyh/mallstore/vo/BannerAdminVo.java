package com.cyh.mallstore.vo;

import com.cyh.mallstore.entity.Banner;
import lombok.Data;

/**
 * 轮播图管理端视图对象
 * 用于管理后台轮播图列表展示，包含完整的管理字段
 */
@Data
public class BannerAdminVo {

    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer sort;
    private Integer status;

    public static BannerAdminVo fromBanner(Banner banner) {
        BannerAdminVo vo = new BannerAdminVo();
        vo.setId(banner.getId());
        vo.setTitle(banner.getTitle());
        vo.setImageUrl(banner.getImageUrl());
        vo.setLinkUrl(banner.getLinkUrl());
        vo.setSort(banner.getSort());
        vo.setStatus(banner.getStatus());
        return vo;
    }
}