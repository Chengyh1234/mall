package com.cyh.malluser.dto;

import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收货地址 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @NotNull(groups = Update.class, message = "地址ID不能为空")
    private Long id;

    @NotBlank(groups = Create.class, message = "收货人不能为空")
    private String receiverName;

    @NotBlank(groups = Create.class, message = "收货人电话不能为空")
    private String receiverPhone;

    @NotBlank(groups = Create.class, message = "省不能为空")
    private String province;

    @NotBlank(groups = Create.class, message = "市不能为空")
    private String city;

    @NotBlank(groups = Create.class, message = "区不能为空")
    private String district;

    @NotBlank(groups = Create.class, message = "详细地址不能为空")
    private String detailAddress;

    private String zipCode;

    private Integer isDefault;
}