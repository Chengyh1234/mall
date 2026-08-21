package com.cyh.mallproduct.dto;

import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 分类新增/修改 DTO
 *
 * <p>分组说明：
 * <ul>
 *   <li>{@link Create}：新增时校验——name 必填</li>
 *   <li>{@link Update}：修改时校验——id 必填</li>
 * </ul>
 * <p>图标处理：
 * <ul>
 *   <li>iconFile：新上传的图标文件</li>
 *   <li>icon：已有图标路径（修改时保留旧图）</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    /** 主键ID（修改时必传） */
    @NotNull(groups = Update.class, message = "分类ID不能为空")
    private Long id;

    /** 分类名称（新增时必填） */
    @NotBlank(groups = {Create.class, Update.class}, message = "分类名称不能为空")
    private String name;

    /** 父级分类ID（0 表示顶级分类） */
    private Long parentId;

    /** 分类层级：1-一级 2-二级 3-三级 */
    private Integer level;

    /** 已有图标路径 */
    private String icon;

    /** 排序（越小越靠前） */
    private Integer sort;

    /** 状态：1-启用 0-禁用 */
    private Integer status;

    /** 新上传的图标文件 */
    private MultipartFile iconFile;
}