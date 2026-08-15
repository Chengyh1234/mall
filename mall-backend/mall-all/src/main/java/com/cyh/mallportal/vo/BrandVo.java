package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.Brand;
import lombok.Data;

/**
 * 品牌 VO
 *
 * 前台公开品牌展示专用，排除 sort/status/isDeleted 等管理字段
 * 对应接口：
 *   GET /brand/detail/{id}
 *   GET /brand/list
 *   GET /brand/page
 *   GET /brand/status/{status}
 *   GET /brand/search
 *   GET /brand/sort
 */
@Data
public class BrandVo {

    /** 品牌ID */
    private Long id;

    /** 品牌名称 */
    private String name;

    /** 品牌Logo路径 */
    private String logo;

    /** 品牌描述 */
    private String description;

    /** 品牌官网 */
    private String website;

    public static BrandVo fromBrand(Brand brand) {
        BrandVo vo = new BrandVo();
        vo.setId(brand.getId());
        vo.setName(brand.getName());
        vo.setLogo(brand.getLogo());
        vo.setDescription(brand.getDescription());
        vo.setWebsite(brand.getWebsite());
        return vo;
    }
}