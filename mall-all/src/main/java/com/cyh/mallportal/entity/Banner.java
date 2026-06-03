package com.cyh.mallportal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 轮播图实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "banners", autoResultMap = true)
public class Banner implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 图片URL
     */
    @TableField(value = "image_url")
    private String imageUrl;

    /**
     * 跳转链接
     */
    @TableField(value = "link_url")
    private String linkUrl;

    /**
     * 排序（越小越靠前）
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 状态（1-启用 0-禁用）
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