package com.cyh.mallportal.vo;

import lombok.Data;

/**
 * 店铺展示 VO
 * 用于公开页面展示店铺信息，仅包含店铺ID、名称、Logo 和描述
 */
@Data
public class StoreVo {

    /** 店铺ID */
    private Long id;

    /** 店铺名称 */
    private String name;

    /** 店铺Logo */
    private String logo;

    /** 店铺描述 */
    private String description;
}