package com.cyh.mallportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 品牌新增/修改DTO
 * 参数说明：
 * - 基本信息：id（修改时必填）, name, logo, description, website, sort, status
 * - Logo（2种方式）：
 *   1. logoFile：新上传的Logo文件
 *   2. logo：已有Logo路径，如 "2026/05/05/uuid_logo.jpg"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandDto {

    private Long id;

    private String name;

    private String logo;

    private String description;

    private String website;

    private Integer sort;

    private Integer status;

    private MultipartFile logoFile;
}
