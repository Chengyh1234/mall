package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.Banner;
import lombok.Data;

/**
 * 轮播图展示 VO
 * 首页轮播图展示专用，仅包含前端需要的字段
 */
@Data
public class BannerVo {

    /** 主键ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 图片URL */
    private String imageUrl;

    /** 跳转链接 */
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