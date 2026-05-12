package com.cyh.mallportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 分类新增/修改DTO
 * 参数说明：
 * - 基本信息：id（修改时必填）, name, parentId, level, sort, status
 * - 图标（2种方式）：
 *   1. iconFile：新上传的图标文件
 *   2. icon：已有图标路径，如 "2026/05/05/uuid_icon.jpg"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;

    private String name;

    private Long parentId;

    private Integer level;

    private String icon;

    private Integer sort;

    private Integer status;

    private MultipartFile iconFile;
}
