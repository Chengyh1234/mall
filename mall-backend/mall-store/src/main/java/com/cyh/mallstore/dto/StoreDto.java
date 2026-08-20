package com.cyh.mallstore.dto;

import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺数据传输对象
 * 用于接收前端提交的店铺信息，含分组校验（新增/更新）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDto {

    @NotNull(groups = Update.class, message = "店铺ID不能为空")
    private Long id;

    @NotBlank(groups = {Create.class, Update.class}, message = "店铺名称不能为空")
    private String name;

    private String logo;

    private String banner;

    private String description;

    private String phone;

    private String address;

    private String businessLicense;

    private Integer status;

    private Integer sort;
}