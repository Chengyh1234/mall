package com.cyh.mallportal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流公司实体类
 * 对应数据库表 logistics_companies
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "logistics_companies", autoResultMap = true)
public class LogisticsCompany implements Serializable {

    /**
     * 物流公司ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 物流公司名称（如：顺丰速运）
     */
    @TableField(value = "name")
    private String name;

    /**
     * 物流公司代码（如：SF）
     */
    @TableField(value = "code")
    private String code;

    /**
     * 物流公司Logo
     */
    @TableField(value = "logo")
    private String logo;

    /**
     * 物流公司官网
     */
    @TableField(value = "website")
    private String website;

    /**
     * 物流公司客服电话
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 排序（越小越靠前）
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 状态：1-启用 0-禁用
     */
    @TableField(value = "status")
    private Integer status;

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