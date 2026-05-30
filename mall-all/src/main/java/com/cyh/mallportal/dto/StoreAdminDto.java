package com.cyh.mallportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺管理员新增/修改DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreAdminDto {
    private Long id;
    private Long storeId;
    private Long userId;
    private Integer role;
    private Integer status;
}