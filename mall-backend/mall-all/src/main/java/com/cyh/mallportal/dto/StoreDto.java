package com.cyh.mallportal.dto;

import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺新增/修改 DTO
 *
 * <p>分组说明：
 * <ul>
 *   <li>{@link Create}：新增时校验——name 必填</li>
 *   <li>{@link Update}：修改时校验——id 必填</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDto {

    /** 主键ID（修改时必传） */
    @NotNull(groups = Update.class, message = "店铺ID不能为空")
    private Long id;

    /** 店铺名称（新增时必填） */
    @NotBlank(groups = {Create.class, Update.class}, message = "店铺名称不能为空")
    private String name;

    /** 店铺Logo（已有路径） */
    private String logo;

    /** 店铺Banner（已有路径） */
    private String banner;

    /** 店铺描述 */
    private String description;

    /** 店铺电话 */
    private String phone;

    /** 店铺地址 */
    private String address;

    /** 营业执照 */
    private String businessLicense;

    /** 状态：1-启用 0-禁用 */
    private Integer status;

    /** 排序（越小越靠前） */
    private Integer sort;
}