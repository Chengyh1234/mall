package com.cyh.mallorder.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallorder.dto.BatchPayDto;
import com.cyh.mallorder.dto.OrderCreateDto;
import com.cyh.mallorder.entity.Order;
import com.cyh.mallorder.service.DistributedLockService;
import com.cyh.mallorder.service.OrderService;
import com.cyh.mallorder.vo.OrderListAdminVo;
import com.cyh.mallorder.vo.OrderListItemVo;
import com.cyh.mallorder.vo.OrderStatusCountVo;
import com.cyh.mallorder.vo.OrderDatailVo;
import com.cyh.mallorder.vo.RefundProgressVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单管理控制器
 * 提供订单的创建、查询、取消、支付、发货、确认收货等功能
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final DistributedLockService distributedLockService;

    /**
     * 创建订单
     *
     * @param orderCreateDto 订单创建DTO
     * @return 创建的订单ID
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public Result<Long> createOrder(@RequestBody @Valid OrderCreateDto orderCreateDto) {
        Long userId = getCurrentUserId();

        if (orderCreateDto.getAddressId() == null && (orderCreateDto.getReceiverName() == null || orderCreateDto.getReceiverPhone() == null)) {
            return Result.error("收货信息不能为空");
        }

        Long orderId = distributedLockService.executeWithLock(
                "lock:order:create:" + userId,
                3, -1, TimeUnit.SECONDS,
                () -> orderService.createOrder(userId, orderCreateDto)
        );
        return Result.success("订单创建成功", orderId);
    }

    /**
     * 从购物车结算创建订单
     *
     * @param addressId    收货地址ID
     * @param buyerMessage 买家留言（可选）
     * @return 创建的订单ID列表
     */
    @PostMapping("/create-from-cart")
    @PreAuthorize("hasRole('USER')")
    public Result<List<Long>> createOrderFromCart(@RequestParam Long addressId,
                                                     @RequestParam(required = false) String buyerMessage) {
        Long userId = getCurrentUserId();

        List<Long> orderIds = distributedLockService.executeWithLock(
                "lock:order:create:cart:" + userId,
                3, -1, TimeUnit.SECONDS,
                () -> orderService.createOrderFromCart(userId, addressId, buyerMessage)
        );
        if (orderIds != null && !orderIds.isEmpty()) {
            return Result.success("订单创建成功", orderIds);
        }
        return Result.error("购物车中没有选中的商品");
    }

    /**
     * 分页获取用户订单列表（带商品明细）
     *
     * @param status   订单状态（可选）
     * @param page     页码
     * @param pageSize 每页数量
     * @return 分页数据
     */
    @GetMapping("/list-with-items")
    @PreAuthorize("hasRole('USER')")
    public Result<Map<String, Object>> getOrderListWithItems(@RequestParam(required = false) Integer status,
                                                             @RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "12") Integer pageSize) {
        Long userId = getCurrentUserId();
        List<OrderListItemVo> list = orderService.getOrderListWithItems(userId, status, page, pageSize);
        int total;
        if (status != null) {
            total = orderService.countOrders(userId, status);
        } else {
            total = orderService.countOrders(userId);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 批量获取用户各状态订单数量
     *
     * @return 各状态订单数量
     */
    @GetMapping("/count-status")
    @PreAuthorize("hasRole('USER')")
    public Result<OrderStatusCountVo> getOrderStatusCount() {
        Long userId = getCurrentUserId();
        OrderStatusCountVo countVo = orderService.countOrderStatusByUserId(userId);
        if (countVo == null) {
            countVo = new OrderStatusCountVo(0, 0, 0, 0);
        }
        return Result.success(countVo);
    }

    /**
     * 根据订单号获取订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/detail/{orderNo}")
    @PreAuthorize("hasRole('USER')")
    public Result<OrderDatailVo> getOrderByOrderNo(@PathVariable String orderNo) {
        Long userId = getCurrentUserId();

        OrderDatailVo orderDatailVo = orderService.getOrderByOrderNo(orderNo);
        if (orderDatailVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderDatailVo.getUserId().equals(userId)) {
            return Result.error("无权查看此订单");
        }
        return Result.success(orderDatailVo);
    }

    /**
     * 取消自己的订单
     *
     * @param orderId      订单ID
     * @param cancelReason 取消原因（可选）
     * @return 取消结果
     */
    @PutMapping("/cancel/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancelOrder(@PathVariable Long orderId,
                                    @RequestParam(required = false) String cancelReason) {
        Long userId = getCurrentUserId();

        OrderDatailVo orderDatailVo = orderService.getOrderByIdForUser(orderId);
        if (orderDatailVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderDatailVo.getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }

        boolean success = orderService.cancelOrder(orderId, cancelReason);
        if (success) {
            return Result.success("订单已取消", null);
        }
        return Result.error("订单取消失败，订单状态不允许");
    }

    /**
     * 支付自己的订单
     *
     * @param orderId 订单ID
     * @param payType 支付方式（alipay/wechat）
     * @return 支付结果
     */
    @PutMapping("/pay/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> payOrder(@PathVariable Long orderId,
                                 @RequestParam(defaultValue = "alipay") String payType) {
        Long userId = getCurrentUserId();

        OrderDatailVo orderDatailVo = orderService.getOrderByIdForUser(orderId);
        if (orderDatailVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderDatailVo.getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }

        Boolean paySuccess = distributedLockService.executeWithLock(
                "lock:order:pay:" + orderId,
                3, -1, TimeUnit.SECONDS,
                () -> orderService.payOrder(orderId, payType)
        );
        if (Boolean.FALSE.equals(paySuccess)) {
            return Result.error("支付失败，订单状态不允许");
        }
        return Result.success("支付成功", null);
    }

    /**
     * 批量付款
     *
     * @param batchPayDto 批量付款请求体
     * @return 批量付款结果汇总
     */
    @PostMapping("/batch-pay")
    @PreAuthorize("hasRole('USER')")
    public Result<Map<String, Object>> batchPayOrders(@RequestBody @Valid BatchPayDto batchPayDto) {
        Long userId = getCurrentUserId();

        Map<String, Object> result = distributedLockService.executeWithLock(
                "lock:order:batch-pay:" + userId,
                3, -1, TimeUnit.SECONDS,
                () -> orderService.batchPayOrders(batchPayDto.getOrderIds(),
                        batchPayDto.getPayType() != null ? batchPayDto.getPayType() : "alipay", userId)
        );
        return Result.success("批量付款处理完成", result);
    }

    /**
     * 确认收货
     *
     * @param orderId 订单ID
     * @return 确认结果
     */
    @PutMapping("/confirm/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> confirmOrder(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();

        OrderDatailVo orderDatailVo = orderService.getOrderByIdForUser(orderId);
        if (orderDatailVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderDatailVo.getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }

        boolean success = orderService.confirmOrder(orderId);
        if (success) {
            return Result.success("确认收货成功", null);
        }
        return Result.error("确认收货失败，订单状态不允许");
    }

    /**
     * 删除订单
     *
     * @param orderId 订单ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> deleteOrder(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();

        OrderDatailVo orderDatailVo = orderService.getOrderByIdForUser(orderId);
        if (orderDatailVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderDatailVo.getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }

        boolean success = orderService.deleteOrder(orderId);
        if (success) {
            return Result.success("订单已删除", null);
        }
        return Result.error("订单删除失败");
    }

    /**
     * 用户申请退款
     *
     * @param orderId      订单ID
     * @param refundReason 退款原因
     * @return 操作结果
     */
    @PostMapping("/refund/apply/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> applyRefund(@PathVariable Long orderId,
                                    @RequestParam(required = false) String refundReason) {
        Long userId = getCurrentUserId();

        OrderDatailVo orderDatailVo = orderService.getOrderByIdForUser(orderId);
        if (orderDatailVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderDatailVo.getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }

        boolean success = orderService.applyRefund(orderId, refundReason);
        if (success) {
            return Result.success("退款申请已提交", null);
        }
        return Result.error("退款申请失败，仅限已支付的订单");
    }

    /**
     * 用户取消退款申请
     *
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PutMapping("/refund/cancel/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> cancelRefund(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();

        OrderDatailVo orderDatailVo = orderService.getOrderByIdForUser(orderId);
        if (orderDatailVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderDatailVo.getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }

        boolean success = orderService.cancelRefund(orderId, userId);
        if (success) {
            return Result.success("退款申请已取消，订单已恢复", null);
        }
        return Result.error("取消失败，当前订单状态不允许取消退款");
    }

    // ==================== 商家操作 ====================

    /**
     * 卖家分页查询店铺订单列表（带商品明细，多条件筛选）
     *
     * @param status    订单状态（可选）
     * @param userId    用户ID（可选）
     * @param orderNo   订单号（可选）
     * @param payTimeStart    支付时间范围-起始
     * @param payTimeEnd      支付时间范围-结束
     * @param deliveryTimeStart 发货时间范围-起始
     * @param deliveryTimeEnd   发货时间范围-结束
     * @param receiveTimeStart 收货时间范围-起始
     * @param receiveTimeEnd   收货时间范围-结束
     * @param page     页码
     * @param pageSize 每页数量
     * @return 分页数据
     */
    @GetMapping("/seller/list")
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> getSellerOrderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime payTimeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime payTimeEnd,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime deliveryTimeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime deliveryTimeEnd,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime receiveTimeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime receiveTimeEnd,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long sellerId = getCurrentUserId();

        List<OrderListItemVo> list = orderService.getSellerOrderListWithItems(
                sellerId, status, userId, orderNo,
                payTimeStart, payTimeEnd, deliveryTimeStart, deliveryTimeEnd,
                receiveTimeStart, receiveTimeEnd, page, pageSize);

        int total = orderService.countSellerOrdersByFilters(
                sellerId, status, userId, orderNo,
                payTimeStart, payTimeEnd, deliveryTimeStart, deliveryTimeEnd,
                receiveTimeStart, receiveTimeEnd);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 商家查询订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/seller/detail/{orderNo}")
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<OrderDatailVo> getOrderDetailBySellerId(@PathVariable String orderNo) {
        Long sellerId = getCurrentUserId();

        OrderDatailVo orderDatailVo = orderService.getOrderDetailBySellerId(orderNo, sellerId);
        if (orderDatailVo == null) {
            return Result.error("订单不存在或不属于您的店铺");
        }
        return Result.success(orderDatailVo);
    }

    /**
     * 发货（商家操作）
     *
     * @param orderId         订单ID
     * @param deliveryCompany 物流公司
     * @param deliveryNo      物流单号
     * @return 发货结果
     */
    @PutMapping("/deliver/{orderId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> deliverOrder(@PathVariable Long orderId,
                                     @RequestParam String deliveryCompany,
                                     @RequestParam String deliveryNo) {
        boolean success = orderService.deliverOrder(orderId, deliveryCompany, deliveryNo);
        if (success) {
            return Result.success("发货成功", null);
        }
        return Result.error("发货失败，订单状态不允许");
    }

    // ==================== 运营管理员操作 ====================

    /**
     * 管理员分页查询全部订单列表（多条件筛选）
     *
     * @param status    订单状态（可选）
     * @param userId    用户ID（可选）
     * @param orderNo   订单号（可选）
     * @param payTimeStart    支付时间范围-起始
     * @param payTimeEnd      支付时间范围-结束
     * @param deliveryTimeStart 发货时间范围-起始
     * @param deliveryTimeEnd   发货时间范围-结束
     * @param receiveTimeStart 收货时间范围-起始
     * @param receiveTimeEnd   收货时间范围-结束
     * @param page     页码
     * @param pageSize 每页数量
     * @return 分页数据
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> getAllOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime payTimeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime payTimeEnd,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime deliveryTimeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime deliveryTimeEnd,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime receiveTimeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime receiveTimeEnd,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Order> orders = orderService.getAllOrders(status, userId, orderNo,
                payTimeStart, payTimeEnd, deliveryTimeStart, deliveryTimeEnd,
                receiveTimeStart, receiveTimeEnd, page, pageSize);
        int total = orderService.countAllOrders(status, userId, orderNo,
                payTimeStart, payTimeEnd, deliveryTimeStart, deliveryTimeEnd,
                receiveTimeStart, receiveTimeEnd);

        List<OrderListAdminVo> voList = orders.stream()
                .map(OrderListAdminVo::fromOrder)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", voList);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 管理员根据订单号查询订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/admin/detail/{orderNo}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<OrderDatailVo> getOrderDetailByAdmin(@PathVariable String orderNo) {
        OrderDatailVo orderDatailVo = orderService.getOrderDetailByOrderNoForAdmin(orderNo);
        if (orderDatailVo == null) {
            return Result.error("订单不存在");
        }
        return Result.success(orderDatailVo);
    }

    /**
     * 管理员强制取消订单
     *
     * @param orderId      订单ID
     * @param cancelReason 取消原因（可选）
     * @return 操作结果
     */
    @PutMapping("/admin/cancel/{orderId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> adminCancelOrder(@PathVariable Long orderId,
                                         @RequestParam(required = false) String cancelReason) {
        boolean success = orderService.adminCancelOrder(orderId, cancelReason);
        if (success) {
            return Result.success("订单已强制取消", null);
        }
        return Result.error("订单取消失败，订单不存在");
    }

    /**
     * 管理员调整订单金额
     *
     * @param orderId        订单ID
     * @param freightAmount  调整运费（可选）
     * @param discountAmount 调整优惠金额（可选）
     * @param payAmount      调整后实付金额（必填）
     * @return 操作结果
     */
    @PutMapping("/admin/adjust/{orderId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> adjustOrderAmount(@PathVariable Long orderId,
                                          @RequestParam(required = false) BigDecimal freightAmount,
                                          @RequestParam(required = false) BigDecimal discountAmount,
                                          @RequestParam BigDecimal payAmount) {
        if (payAmount == null) {
            return Result.error("实付金额不能为空");
        }
        boolean success = orderService.adjustOrderAmount(orderId, freightAmount, discountAmount, payAmount);
        if (success) {
            return Result.success("订单金额已调整", null);
        }
        return Result.error("金额调整失败，仅限待付款订单");
    }

    /**
     * 管理员审核通过退款
     *
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PutMapping("/refund/approve/{orderId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> approveRefund(@PathVariable Long orderId) {
        Long operatorId = getCurrentUserId();
        boolean success = orderService.approveRefund(orderId, operatorId);
        if (success) {
            return Result.success("退款审核通过，已完成退款", null);
        }
        return Result.error("审核通过失败，订单状态异常");
    }

    /**
     * 管理员拒接退款
     *
     * @param orderId      订单ID
     * @param rejectReason 拒绝原因（必填）
     * @return 操作结果
     */
    @PutMapping("/refund/reject/{orderId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> rejectRefund(@PathVariable Long orderId,
                                     @RequestParam String rejectReason) {
        Long operatorId = getCurrentUserId();
        boolean success = orderService.rejectRefund(orderId, rejectReason, operatorId);
        if (success) {
            return Result.success("退款已拒绝", null);
        }
        return Result.error("拒接失败，订单状态异常");
    }

    /**
     * 查询退款进度
     *
     * @param orderId 订单ID
     * @return 退款进度信息
     */
    @GetMapping("/refund/progress/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<RefundProgressVo> getRefundProgress(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();

        OrderDatailVo orderDatailVo = orderService.getOrderById(orderId);
        if (orderDatailVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderDatailVo.getUserId().equals(userId) && !isAdminUser()) {
            return Result.error("无权查看此订单");
        }

        RefundProgressVo progress = orderService.getRefundProgress(orderId);
        if (progress == null) {
            return Result.error("订单不存在");
        }
        return Result.success(progress);
    }

    /**
     * 获取支付状态描述（公开接口）
     *
     * @param payStatus 支付状态
     * @return 支付状态描述
     */
    @GetMapping("/pay-status-desc/{payStatus}")
    public Result<Map<String, String>> getPayStatusDesc(@PathVariable Integer payStatus) {
        String desc = orderService.getPayStatusDesc(payStatus);
        Map<String, String> result = new HashMap<>();
        result.put("payStatus", String.valueOf(payStatus));
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