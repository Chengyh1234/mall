package com.cyh.mallstore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 轮播图实体
 * 对应数据库 banners 表，管理首页/店铺页的轮播广告图片
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "banners", autoResultMap = true)
public class Banner implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "title")
    private String title;

    @TableField(value = "image_url")
    private String imageUrl;

    @TableField(value = "link_url")
    private String linkUrl;

    @TableField(value = "sort")
    private Integer sort;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}