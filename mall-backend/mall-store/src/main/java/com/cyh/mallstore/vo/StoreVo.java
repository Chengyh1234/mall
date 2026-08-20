package com.cyh.mallstore.vo;

import lombok.Data;

/**
 * 店铺公开视图对象
 * 用于前端店铺列表展示，仅包含公开信息
 */
@Data
public class StoreVo {

    private Long id;

    private String name;

    private String logo;

    private String description;
}