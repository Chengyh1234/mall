package com.cyh.mallportal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "stores", autoResultMap = true)
public class Store implements Serializable {

    /**
     * 店铺ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 店铺名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 商家用户ID
     */
    @TableField(value = "seller_id")
    private Long sellerId;

    /**
     * 店铺Logo
     */
    @TableField(value = "logo")
    private String logo;

    /**
     * 店铺横幅
     */
    @TableField(value = "banner")
    private String banner;

    /**
     * 店铺描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 联系电话
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 店铺地址
     */
    @TableField(value = "address")
    private String address;

    /**
     * 营业执照
     */
    @TableField(value = "business_license")
    private String businessLicense;

    /**
     * 状态: 1-正常 0-禁用 2-审核中 3-审核失败
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 审核驳回原因
     */
    @TableField(value = "reject_reason")
    private String rejectReason;

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