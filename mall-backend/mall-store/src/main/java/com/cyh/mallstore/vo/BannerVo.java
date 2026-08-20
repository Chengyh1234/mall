package com.cyh.mallstore.vo;

import com.cyh.mallstore.entity.Banner;
import lombok.Data;

/**
 * 轮播图公开视图对象
 * 用于前端展示活跃轮播图，仅包含公开的展示信息
 */
@Data
public class BannerVo {

    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;

    public static BannerVo fromBanner(Banner banner) {
        BannerVo vo = new BannerVo();
        vo.setId(banner.getId());
        vo.setTitle(banner.getTitle());
        vo.setImageUrl(banner.getImageUrl());
        vo.setLinkUrl(banner.getLinkUrl());
        return vo;
    }
}