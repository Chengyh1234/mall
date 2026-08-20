package com.cyh.mallstore.dto;

import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺管理员数据传输对象
 * 用于接收前端提交的管理员信息，含分组校验
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreAdminDto {

    @NotNull(groups = Update.class, message = "管理员ID不能为空")
    private Long id;

    @NotNull(groups = Create.class, message = "店铺ID不能为空")
    private Long storeId;

    @NotNull(groups = Create.class, message = "用户ID不能为空")
    private Long userId;

    @Min(value = 1, groups = {Create.class, Update.class}, message = "角色值不能小于1")
    @Max(value = 4, groups = {Create.class, Update.class}, message = "角色值不能大于4")
    private Integer role;

    private Integer status;
}