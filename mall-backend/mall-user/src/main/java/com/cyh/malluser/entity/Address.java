package com.cyh.malluser.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收货地址实体类
 * 对应数据库表 addresses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "addresses", autoResultMap = true)
public class Address implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 收货人
     */
    @TableField(value = "receiver_name")
    private String receiverName;

    /**
     * 收货人电话
     */
    @TableField(value = "receiver_phone")
    private String receiverPhone;

    /**
     * 省
     */
    @TableField(value = "province")
    private String province;

    /**
     * 市
     */
    @TableField(value = "city")
    private String city;

    /**
     * 区
     */
    @TableField(value = "district")
    private String district;

    /**
     * 详细地址
     */
    @TableField(value = "detail_address")
    private String detailAddress;

    /**
     * 邮编
     */
    @TableField(value = "zip_code")
    private String zipCode;

    /**
     * 是否默认: 1-是 0-否
     */
    @TableField(value = "is_default")
    private Integer isDefault;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}