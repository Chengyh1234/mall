package com.cyh.mallportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
/**
 * 商品新增/修改DTO
 * 参数说明：
 * - 基本信息：id（修改时必填）, name, categoryId, brandId, description, unit, keywords, sales, status
 * - 主图（2种方式）：
 *   1. mainImageName：指定上传文件中的哪个作为主图，如 "main.jpg"
 *   2. mainImage：已有主图路径，如 "2026/05/05/uuid_main.jpg"
 * - 图片集（2种方式）：
 *   1. imageFiles：新上传的图片文件列表
 *   2. images：已有图片路径JSON数组，如 ["2026/05/05/a.jpg","2026/05/05/b.jpg"]。为图片列表，主图不包含在内
 * - keepOldImages：是否保留全部旧图片，默认false（删除旧图片）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpuDto {
    private Long id;
    private Long storeId;
    private String name;
    private Long categoryId;
    private Long brandId;
    private String description;
    private String unit;
    private String keywords;
    private Integer sales;
    private Integer status;
    private String mainImage;
    private String mainImageName;
    private String images;
    private List<MultipartFile> imageFiles;
    private Boolean keepOldImages = false;
}
