package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.dto.OrderDeliveryDto;
import com.cyh.mallportal.entity.OrderDelivery;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.service.OrderDeliveryService;
import com.cyh.mallportal.service.OrderService;
import com.cyh.mallportal.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发货记录管理控制器
 * 提供发货记录的增删改查及物流状态更新功能
 */
@RestController
@RequestMapping("/delivery")
public class OrderDeliveryController {

    @Autowired
    private OrderDeliveryService orderDeliveryService;

    @Autowired
    private OrderService orderService;

    /**
     * 创建发货记录（管理员操作）
     *
     * @param deliveryDto 发货记录DTO
     * @return 创建结果
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('order:delivery:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> createDelivery(@RequestBody OrderDeliveryDto deliveryDto) {
        // 参数校验
        if (deliveryDto.getOrderId() == null) {
            return Result.error("订单ID不能为空");
        }
        if (deliveryDto.getDeliveryCompany() == null || deliveryDto.getDeliveryCompany().trim().isEmpty()) {
            return Result.error("物流公司不能为空");
        }
        if (deliveryDto.getDeliveryNo() == null || deliveryDto.getDeliveryNo().trim().isEmpty()) {
            return Result.error("物流单号不能为空");
        }

        Long deliveryId = orderDeliveryService.createDelivery(deliveryDto);
        if (deliveryId != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", deliveryId);
            return Result.success("发货记录创建成功", data);
        }
        return Result.error("发货记录创建失败");
    }

    /**
     * 签收发货记录（管理员或用户操作）
     * 管理员可签收任何订单，用户只能签收自己的订单
     *
     * @param deliveryNo 物流单号
     * @param signer     签收人（可选）
     * @return 签收结果
     */
    @PutMapping("/sign")
    @PreAuthorize("hasAuthority('order:delivery:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> signDelivery(@RequestParam String deliveryNo,
                                     @RequestParam(required = false) String signer) {
        // 如果用户不是管理员，验证是否是订单所有者
        Long userId = getCurrentUserId();
        boolean isAdmin = isAdminUser();

        if (!isAdmin && userId != null) {
            OrderDelivery delivery = orderDeliveryService.getByDeliveryNo(deliveryNo);
            if (delivery != null) {
                OrderVo orderVo = orderService.getOrderById(delivery.getOrderId());
                if (orderVo != null && !orderVo.getUserId().equals(userId)) {
                    return Result.error("无权操作此订单");
                }
            }
        }

        boolean success = orderDeliveryService.signDelivery(deliveryNo, signer);
        if (success) {
            return Result.success("签收成功", null);
        }
        return Result.error("签收失败，发货记录不存在");
    }

    /**
     * 标记物流异常（管理员操作）
     *
     * @param deliveryNo      物流单号
     * @param exceptionReason 异常原因
     * @return 处理结果
     */
    @PutMapping("/exception")
    @PreAuthorize("hasAuthority('order:delivery:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> exceptionDelivery(@RequestParam String deliveryNo,
                                          @RequestParam String exceptionReason) {
        boolean success = orderDeliveryService.exceptionDelivery(deliveryNo, exceptionReason);
        if (success) {
            return Result.success("物流异常处理成功", null);
        }
        return Result.error("处理失败，发货记录不存在");
    }

    /**
     * 根据订单ID获取发货记录列表
     * 验证用户是否是订单所有者
     *
     * @param orderId 订单ID
     * @return 发货记录列表
     */
    @GetMapping("/list/{orderId}")
    public Result<List<OrderDelivery>> getByOrderId(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        boolean isAdmin = isAdminUser();

        // 非管理员需要验证订单归属
        if (!isAdmin && userId != null) {
            OrderVo orderVo = orderService.getOrderById(orderId);
            if (orderVo != null && !orderVo.getUserId().equals(userId)) {
                return Result.error("无权查看此订单的发货记录");
            }
        }

        List<OrderDelivery> deliveries = orderDeliveryService.getByOrderId(orderId);
        return Result.success(deliveries);
    }

    /**
     * 根据物流单号获取发货记录
     *
     * @param deliveryNo 物流单号
     * @return 发货记录
     */
    @GetMapping("/detail/{deliveryNo}")
    public Result<OrderDelivery> getByDeliveryNo(@PathVariable String deliveryNo) {
        OrderDelivery delivery = orderDeliveryService.getByDeliveryNo(deliveryNo);
        if (delivery != null) {
            return Result.success(delivery);
        }
        return Result.error("发货记录不存在");
    }

    /**
     * 根据状态获取发货记录列表（管理员操作）
     *
     * @param deliveryStatus 发货状态
     * @return 发货记录列表
     */
    @GetMapping("/status/{deliveryStatus}")
    @PreAuthorize("hasAuthority('order:delivery:query') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<List<OrderDelivery>> getByStatus(@PathVariable Integer deliveryStatus) {
        List<OrderDelivery> deliveries = orderDeliveryService.getByStatus(deliveryStatus);
        return Result.success(deliveries);
    }

    /**
     * 获取发货状态描述
     *
     * @param deliveryStatus 发货状态
     * @return 状态描述
     */
    @GetMapping("/status-desc/{deliveryStatus}")
    public Result<Map<String, String>> getStatusDesc(@PathVariable Integer deliveryStatus) {
        String desc = orderDeliveryService.getStatusDesc(deliveryStatus);
        Map<String, String> result = new HashMap<>();
        result.put("deliveryStatus", String.valueOf(deliveryStatus));
        result.put("desc", desc);
        return Result.success(result);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getId();
            }
        }
        return null;
    }

    /**
     * 判断当前用户是否是管理员
     */
    private boolean isAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().contains("SUPER_ADMIN") ||
                                   a.getAuthority().contains("ADMIN") ||
                                   a.getAuthority().contains("SELLER"));
        }
        return false;
    }
}