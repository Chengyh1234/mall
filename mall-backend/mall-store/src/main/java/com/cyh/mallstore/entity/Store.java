package com.cyh.mallstore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺实体
 * 对应数据库 stores 表，包含店铺基本信息、资质、审核状态等
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "stores", autoResultMap = true)
public class Store implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "seller_id")
    private Long sellerId;

    @TableField(value = "logo")
    private String logo;

    @TableField(value = "banner")
    private String banner;

    @TableField(value = "description")
    private String description;

    @TableField(value = "phone")
    private String phone;

    @TableField(value = "address")
    private String address;

    @TableField(value = "business_license")
    private String businessLicense;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "sort")
    private Integer sort;

    @TableField(value = "reject_reason")
    private String rejectReason;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}