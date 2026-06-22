package com.cyh.mallportal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建DTO
 * 参数说明：
 * - 收货信息：addressId（地址ID）或 receiverName/receiverPhone/receiverAddress
 * - 商品列表：items
 * - 金额信息：totalAmount, discountAmount, freightAmount, payAmount
 * - 其他：remark
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDto {

    /** 收货地址ID */
    private Long addressId;

    /** 收货人 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 收货地址（省市区+详细地址） */
    private String receiverAddress;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 运费 */
    private BigDecimal freightAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 订单备注 */
    private String remark;

    /** 订单项列表 */
    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderItemDto> items;
}