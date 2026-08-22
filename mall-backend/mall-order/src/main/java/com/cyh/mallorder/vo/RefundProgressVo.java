package com.cyh.mallorder.vo;

/**
 * 退款进度 VO
 * 返回给前端查看退款申请的审核进度和状态信息
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款进度VO
 * 用于展示用户退款申请的审核状态和详细信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundProgressVo {

    /**
     * 退款状态码
     * 0=未申请退款 1=退款中 2=已退款 3=已拒绝
     */
    private Integer refundStatusCode;

    /**
     * 退款状态描述
     * 如"未申请退款"、"退款中"、"已退款"、"已拒绝"
     */
    private String refundStatusDesc;

    /**
     * 退款原因（用户申请时填写）
     */
    private String refundReason;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 拒绝原因（管理员审核拒绝时填写）
     */
    private String rejectReason;

    /**
     * 退款申请时间
     */
    private LocalDateTime applyTime;

    /**
     * 审核/完成时间（退款完成或拒绝的时间）
     */
    private LocalDateTime reviewTime;
}