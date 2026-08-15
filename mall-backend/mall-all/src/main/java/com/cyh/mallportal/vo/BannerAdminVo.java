package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.Banner;
import lombok.Data;

/**
 * 轮播图管理 VO（管理后台）
 *
 * 管理后台轮播图列表展示专用，包含 sort 和 status 字段便于管理
 * 对应接口：
 *   GET /banner/list
 */
@Data
public class BannerAdminVo {

    /** 主键ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 图片URL */
    private String imageUrl;

    /** 跳转链接 */
    private String linkUrl;

    /** 排序（越小越靠前） */
    private Integer sort;

    /** 状态（1-启用 0-禁用） */
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