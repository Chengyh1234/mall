package com.cyh.mallcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 店铺信息 DTO（Feign 内部通信）
 * <p>
 * 用于 mall-product 通过 Feign 调用 mall-store 时传递店铺数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreInfoVo {

    private Long id;
    private String name;
    private String logo;
    private String banner;
    private String description;
    private String address;
    private LocalDateTime createdAt;
}