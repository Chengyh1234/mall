package com.cyh.mallportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 店铺新增/修改DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDto {
    private Long id;
    private String name;
    private String logo;
    private String banner;
    private String description;
    private String phone;
    private String address;
    private String businessLicense;
    private Integer status;
    private Integer sort;
}