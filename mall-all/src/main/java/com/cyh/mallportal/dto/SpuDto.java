package com.cyh.mallportal.dto;

import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 商品（SPU）新增/修改 DTO
 *
 * <p>分组说明：
 * <ul>
 *   <li>{@link Create}：新增时校验——name、categoryId 必填</li>
 *   <li>{@link Update}：修改时校验——id 必填</li>
 * </ul>
 * <p>图片处理：
 * <ul>
 *   <li>mainImageName：指定上传文件中的哪个作为主图</li>
 *   <li>mainImage：已有主图路径（修改时保留旧图）</li>
 *   <li>images：已有图片路径 JSON 数组（修改时保留旧图）</li>
 *   <li>imageFiles：新上传的图片文件</li>
 *   <li>keepOldImages：是否保留全部旧图片，默认 false</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpuDto {

    /** 主键ID（修改时必传） */
    @NotNull(groups = Update.class, message = "商品ID不能为空")
    private Long id;

    /** 所属店铺ID（由系统自动设置，前端无需传入） */
    private Long storeId;

    /** 商品名称（新增时必填） */
    @NotBlank(groups = {Create.class, Update.class}, message = "商品名称不能为空")
    private String name;

    /** 分类ID（新增时必填） */
    @NotNull(groups = Create.class, message = "分类ID不能为空")
    private Long categoryId;

    /** 品牌ID */
    private Long brandId;

    /** 商品描述 */
    private String description;

    /** 单位（如：件、个、箱） */
    private String unit;

    /** 关键词（SEO 用，逗号分隔） */
    private String keywords;

    /** 销量 */
    private Integer sales;

    /** 状态：1-上架 0-下架 */
    private Integer status;

    /** 主图路径（已有） */
    private String mainImage;

    /** 主图文件名（指定上传文件中的哪个作为主图） */
    private String mainImageName;

    /** 已有图片路径 JSON 数组（主图不包含在内） */
    private String images;

    /** 新上传的图片文件列表 */
    private List<MultipartFile> imageFiles;

    /** 是否保留全部旧图片（默认 false 表示删除旧图片） */
    private Boolean keepOldImages = false;
}