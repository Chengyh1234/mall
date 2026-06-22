package com.cyh.mallportal.dto;

import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 品牌新增/修改 DTO
 *
 * <p>分组说明：
 * <ul>
 *   <li>{@link Create}：新增时校验——name 必填</li>
 *   <li>{@link Update}：修改时校验——id 必填</li>
 * </ul>
 * <p>Logo 处理：
 * <ul>
 *   <li>logoFile：新上传的 Logo 文件</li>
 *   <li>logo：已有 Logo 路径（修改时保留旧图）</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandDto {

    /** 主键ID（修改时必传） */
    @NotNull(groups = Update.class, message = "品牌ID不能为空")
    private Long id;

    /** 品牌名称（新增时必填） */
    @NotBlank(groups = {Create.class, Update.class}, message = "品牌名称不能为空")
    private String name;

    /** 已有 Logo 路径 */
    private String logo;

    /** 品牌描述 */
    private String description;

    /** 品牌官网 */
    private String website;

    /** 排序（越小越靠前） */
    private Integer sort;

    /** 状态：1-启用 0-禁用 */
    private Integer status;

    /** 新上传的 Logo 文件 */
    private MultipartFile logoFile;
}