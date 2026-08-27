package com.cyh.mallorder.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallorder.dto.OrderDeliveryDto;
import com.cyh.mallorder.entity.OrderDelivery;
import com.cyh.mallorder.service.OrderDeliveryService;
import com.cyh.mallorder.service.OrderService;
import com.cyh.mallorder.vo.OrderDatailVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
public class OrderDeliveryController {

    private final OrderDeliveryService orderDeliveryService;
    private final OrderService orderService;

    /**
     * 创建发货记录（管理员/商家操作）
     *
     * @param deliveryDto 发货记录DTO
     * @return 创建结果
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> createDelivery(@RequestBody @Valid OrderDeliveryDto deliveryDto) {
        Long deliveryId = orderDeliveryService.createDelivery(deliveryDto);
        if (deliveryId != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", deliveryId);
            return Result.success("发货记录创建成功", data);
        }
        return Result.error("发货记录创建失败");
    }

    /**
     * 签收发货记录
     *
     * @param deliveryNo 物流单号
     * @param signer     签收人（可选）
     * @return 签收结果
     */
    @PutMapping("/sign")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> signDelivery(@RequestParam String deliveryNo,
                                     @RequestParam(required = false) String signer) {
        Long userId = getCurrentUserId();
        boolean isAdmin = isAdminUser();

        if (!isAdmin && userId != null) {
            OrderDelivery delivery = orderDeliveryService.getByDeliveryNo(deliveryNo);
            if (delivery != null) {
                OrderDatailVo orderDatailVo = orderService.getOrderById(delivery.getOrderId());
                if (orderDatailVo != null && !orderDatailVo.getUserId().equals(userId)) {
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
     * 标记物流异常
     *
     * @param deliveryNo      物流单号
     * @param exceptionReason 异常原因
     * @return 处理结果
     */
    @PutMapping("/exception")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
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
     *
     * @param orderId 订单ID
     * @return 发货记录列表
     */
    @GetMapping("/list/{orderId}")
    public Result<List<OrderDelivery>> getByOrderId(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        boolean isAdmin = isAdminUser();

        if (!isAdmin && userId != null) {
            OrderDatailVo orderDatailVo = orderService.getOrderById(orderId);
            if (orderDatailVo != null && !orderDatailVo.getUserId().equals(userId)) {
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
     * 根据状态获取发货记录列表
     *
     * @param deliveryStatus 发货状态
     * @return 发货记录列表
     */
    @GetMapping("/status/{deliveryStatus}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
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
     * 获取当前登录用户 ID
     * 从网关透传的 X-User-Id 请求头中提取，由 GatewayHeaderAuthenticationFilter 注入 SecurityContext
     *
     * @return 用户 ID，未登录或解析失败时返回 null
     */
    protected Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                try {
                    return Long.valueOf((String) principal);
                } catch (NumberFormatException e) {
                    log.warn("解析用户 ID 失败, principal: {}", principal);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 判断当前用户是否是管理员或商家
     * 包含 SUPER_ADMIN（超级管理员）、SELLER（商家）、STORE_ADMIN（店铺管理员）
     *
     * @return true 表示具有管理员/商家权限
     */
    protected boolean isAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().contains("SUPER_ADMIN") ||
                            a.getAuthority().contains("SELLER") ||
                            a.getAuthority().contains("STORE_ADMIN"));
        }
        return false;
    }
    }