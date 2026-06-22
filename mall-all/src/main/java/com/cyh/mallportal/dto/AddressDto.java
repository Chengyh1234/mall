package com.cyh.mallportal.dto;

import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收货地址 DTO
 *
 * <p>分组说明：
 * <ul>
 *   <li>{@link Create}：新增时校验——收货人、电话、省市区域、详细地址必填</li>
 *   <li>{@link Update}：修改时校验——id 必填</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    /** 地址ID（更新时必传） */
    @NotNull(groups = Update.class, message = "地址ID不能为空")
    private Long id;

    /** 收货人 */
    @NotBlank(groups = Create.class, message = "收货人不能为空")
    private String receiverName;

    /** 收货人电话 */
    @NotBlank(groups = Create.class, message = "收货人电话不能为空")
    private String receiverPhone;

    /** 省 */
    @NotBlank(groups = Create.class, message = "省不能为空")
    private String province;

    /** 市 */
    @NotBlank(groups = Create.class, message = "市不能为空")
    private String city;

    /** 区 */
    @NotBlank(groups = Create.class, message = "区不能为空")
    private String district;

    /** 详细地址 */
    @NotBlank(groups = Create.class, message = "详细地址不能为空")
    private String detailAddress;

    /** 邮编 */
    private String zipCode;

    /** 是否默认: 1-是 0-否 */
    private Integer isDefault;
}