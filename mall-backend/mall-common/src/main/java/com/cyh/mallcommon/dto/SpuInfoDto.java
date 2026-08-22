package com.cyh.mallcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SPU 信息 DTO（Feign 内部通信）
 * <p>
 * 用于 mall-order 通过 Feign 调用 mall-product 时传递 SPU 数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpuInfoDto {

    /** SPU ID */
    private Long id;

    /** SPU 名称 */
    private String name;

    /** 商家 ID */
    private Long sellerId;
}