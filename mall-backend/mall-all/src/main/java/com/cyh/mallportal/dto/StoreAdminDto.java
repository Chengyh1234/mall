package com.cyh.mallportal.dto;

import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺管理员新增/修改 DTO
 *
 * <p>分组说明：
 * <ul>
 *   <li>{@link Create}：新增时校验——storeId、userId 必填，role 范围校验</li>
 *   <li>{@link Update}：修改时校验——id 必填，role 范围校验</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreAdminDto {

    /** 主键ID（更新时必传） */
    @NotNull(groups = Update.class, message = "管理员ID不能为空")
    private Long id;

    /** 店铺ID（新增时必传） */
    @NotNull(groups = Create.class, message = "店铺ID不能为空")
    private Long storeId;

    /** 管理员用户ID（新增时必传） */
    @NotNull(groups = Create.class, message = "用户ID不能为空")
    private Long userId;

    /** 角色: 1-店长 2-管理员 3-客服 4-财务（不传则使用默认值 2） */
    @Min(value = 1, groups = {Create.class, Update.class}, message = "角色值不能小于1")
    @Max(value = 4, groups = {Create.class, Update.class}, message = "角色值不能大于4")
    private Integer role;

    /** 状态: 1-启用 0-禁用（不传则使用默认值 1） */
    private Integer status;
}