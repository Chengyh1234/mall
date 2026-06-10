package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.dto.BatchPayDto;
import com.cyh.mallportal.dto.OrderCreateDto;
import com.cyh.mallportal.entity.Order;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.service.OrderService;
import com.cyh.mallportal.vo.OrderListItemVo;
import com.cyh.mallportal.vo.OrderVo;
import com.cyh.mallportal.vo.RefundProgressVo;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * 订单管理控制器
 * 提供订单的创建、查询、取消、支付、发货、确认收货等功能
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     *
     * @param orderCreateDto 订单创建DTO
     * @return 创建的订单ID
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public Result<Long> createOrder(@RequestBody OrderCreateDto orderCreateDto) {
        Long userId = getCurrentUserId();

        if (orderCreateDto.getItems() == null || orderCreateDto.getItems().isEmpty()) {
            return Result.error("订单商品不能为空");
        }

        if (orderCreateDto.getAddressId() == null && (orderCreateDto.getReceiverName() == null || orderCreateDto.getReceiverPhone() == null)) {
            return Result.error("收货信息不能为空");
        }

        Long orderId = orderService.createOrder(userId, orderCreateDto);
        return Result.success("订单创建成功", orderId);
    }

    /**
     * 从购物车结算创建订单
     * 使用购物车中已选中的商品创建订单，每个 SKU 生成一笔独立订单
     *
     * @param addressId    收货地址ID
     * @param buyerMessage 买家留言（可选）
     * @return 创建的订单ID列表（每个商品一笔独立订单）
     */
    @PostMapping("/create-from-cart")
    @PreAuthorize("hasRole('USER')")
    public Result<List<Long>> createOrderFromCart(@RequestParam Long addressId,
                                                     @RequestParam(required = false) String buyerMessage) {
        Long userId = getCurrentUserId();

        if (addressId == null) {
            return Result.error("收货地址不能为空");
        }

        List<Long> orderIds = orderService.createOrderFromCart(userId, addressId, buyerMessage);
        if (orderIds != null && !orderIds.isEmpty()) {
            return Result.success("订单创建成功", orderIds);
        }
        return Result.error("购物车中没有选中的商品");
    }

    /**
     * 分页获取自己的订单列表（普通用户，自动过滤 is_deleted=0）<已经取消使用这个来展示订单了>
     *
     * @param status   订单状态（可选，用于筛选）
     * @param page     页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页数据：{ list, page, pageSize, total }
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public Result<Map<String, Object>> getOrdersByUserId(@RequestParam(required = false) Integer status,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();

        List<Order> orders;
        int total;
        if (status != null) {
            orders = orderService.getOrdersByUserIdAndStatus(userId, status, page, pageSize);
            total = orderService.countOrders(userId, status);
        } else {
            orders = orderService.getOrdersByUserId(userId, page, pageSize);
            total = orderService.countOrders(userId);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", orders);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 分页获取用户订单列表（带商品明细，自动过滤 is_deleted=0）
     * 返回订单摘要信息 + 订单下所有商品明细
     *
     * @param status   订单状态（可选，传 null 查全部）
     * @param page     页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页数据：{ list: OrderListItemVo[], page, pageSize, total }
     */
    @GetMapping("/list-with-items")
    @PreAuthorize("hasRole('USER')")
    public Result<Map<String, Object>> getOrderListWithItems(@RequestParam(required = false) Integer status,
                                                             @RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        List<OrderListItemVo> list = orderService.getOrderListWithItems(userId, status, page, pageSize);
        // 统计总数，用于前端分页
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
     * 根据订单号获取订单详情（仅限普通用户，自动过滤 is_deleted=0）
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/detail/{orderNo}")
    @PreAuthorize("hasRole('USER')")
    public Result<OrderVo> getOrderByOrderNo(@PathVariable String orderNo) {
        Long userId = getCurrentUserId();

        OrderVo orderVo = orderService.getOrderByOrderNo(orderNo);
        if (orderVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderVo.getOrder().getUserId().equals(userId)) {
            return Result.error("无权查看此订单");
        }
        return Result.success(orderVo);
    }


    /**
     * 取消自己的订单（自动过滤 is_deleted=0）
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

        OrderVo orderVo = orderService.getOrderByIdForUser(orderId);
        if (orderVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderVo.getOrder().getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }

        boolean success = orderService.cancelOrder(orderId, cancelReason);
        if (success) {
            return Result.success("订单已取消", null);
        }
        return Result.error("订单取消失败，订单状态不允许");
    }

    /**
     * 支付自己的订单（自动过滤 is_deleted=0）
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

        OrderVo orderVo = orderService.getOrderByIdForUser(orderId);
        if (orderVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderVo.getOrder().getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }

        boolean success = orderService.payOrder(orderId, payType);
        if (success) {
            return Result.success("支付成功", null);
        }
        return Result.error("支付失败，订单状态不允许");
    }

    /**
     * 批量付款（支持选择多个订单同时支付）
     * 对当前用户的多笔待付款订单进行批量支付，已支付或失败的订单不影响其他订单
     *
     * @param batchPayDto 批量付款请求体，包含订单ID列表和支付方式
     * @return 批量付款结果汇总：成功/失败列表及计数
     */
    @PostMapping("/batch-pay")
    @PreAuthorize("hasRole('USER')")
    public Result<Map<String, Object>> batchPayOrders(@RequestBody BatchPayDto batchPayDto) {
        Long userId = getCurrentUserId();

        // 参数校验
        if (batchPayDto.getOrderIds() == null || batchPayDto.getOrderIds().isEmpty()) {
            return Result.error("订单ID列表不能为空");
        }

        Map<String, Object> result = orderService.batchPayOrders(batchPayDto.getOrderIds(),
                batchPayDto.getPayType() != null ? batchPayDto.getPayType() : "alipay", userId);
        return Result.success("批量付款处理完成", result);
    }

    /**
     * 确认收货自己的订单变为收货
     *
     * @param orderId 订单ID
     * @return 确认结果
     */
    @PutMapping("/confirm/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> confirmOrder(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();

        OrderVo orderVo = orderService.getOrderByIdForUser(orderId);
        if (orderVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderVo.getOrder().getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }

        boolean success = orderService.confirmOrder(orderId);
        if (success) {
            return Result.success("确认收货成功", null);
        }
        return Result.error("确认收货失败，订单状态不允许");
    }

    /**
     * 删除订单,不用删除订单详情，用户能删除自己的订单
     *
     * @param orderId 订单ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> deleteOrder(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();

        OrderVo orderVo = orderService.getOrderByIdForUser(orderId);
        if (orderVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderVo.getOrder().getUserId().equals(userId)) {
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
     * 仅限已支付的订单
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

        // 校验订单归属
        OrderVo orderVo = orderService.getOrderByIdForUser(orderId);
        if (orderVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderVo.getOrder().getUserId().equals(userId)) {
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
     * 仅允许 status=6（退款）的订单取消，取消后恢复到退款前的订单状态
     * 自动过滤 is_deleted=0，已删除订单不可操作
     *
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PutMapping("/refund/cancel/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> cancelRefund(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();

        // 校验订单归属
        OrderVo orderVo = orderService.getOrderByIdForUser(orderId);
        if (orderVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderVo.getOrder().getUserId().equals(userId)) {
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
     * 返回 OrderListItemVo，根据当前登录商家用户ID，通过 order_items → spu 关联链路查询店铺订单
     * 支持按状态、用户ID、订单号、支付时间、发货时间、收货时间等筛选
     *
     * @param status    订单状态（可选）
     * @param userId    用户ID（可选）
     * @param orderNo   订单号（可选，模糊匹配）
     * @param payTimeStart    支付时间范围-起始（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param payTimeEnd      支付时间范围-结束（可选）
     * @param deliveryTimeStart 发货时间范围-起始（可选）
     * @param deliveryTimeEnd   发货时间范围-结束（可选）
     * @param receiveTimeStart 收货时间范围-起始（可选）
     * @param receiveTimeEnd   收货时间范围-结束（可选）
     * @param page     页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页数据：{ list(OrderListItemVo[]), page, pageSize, total }
     */
    @GetMapping("/seller/list")
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
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

        // 查询带商品明细的订单列表
        List<OrderListItemVo> list = orderService.getSellerOrderListWithItems(
                sellerId, status, userId, orderNo,
                payTimeStart, payTimeEnd, deliveryTimeStart, deliveryTimeEnd,
                receiveTimeStart, receiveTimeEnd, page, pageSize);

        // 统计总数
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
     * 校验订单是否属于当前商家（通过 order_items → spu.seller_id），仅返回本店铺的订单
     * 不过滤 is_deleted，商家可查看包括已删除的订单
     *
     * @param orderNo 订单号
     * @return 订单详情（含订单项和发货记录）
     */
    @GetMapping("/seller/detail/{orderNo}")
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<OrderVo> getOrderDetailBySellerId(@PathVariable String orderNo) {
        Long sellerId = getCurrentUserId();

        OrderVo orderVo = orderService.getOrderDetailBySellerId(orderNo, sellerId);
        if (orderVo == null) {
            return Result.error("订单不存在或不属于您的店铺");
        }
        return Result.success(orderVo);
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


    //==========不知道是否需要===
    /**
     * 获取订单状态描述（公开接口）？？？？？？？？？？？？
     *
     * @param status 订单状态
     * @return 状态描述
     */
    @GetMapping("/status-desc/{status}")
    public Result<Map<String, String>> getStatusDesc(@PathVariable Integer status) {
        String desc = orderService.getStatusDesc(status);
        Map<String, String> result = new HashMap<>();
        result.put("status", String.valueOf(status));
        result.put("desc", desc);
        return Result.success(result);
    }

    /**
     * 获取支付状态描述（公开接口）？？？？？？？？？？？？？？？
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


    // ==================== 运营管理员操作 ====================

    /**
     * 管理员分页查询全部订单列表（多条件筛选）
     * 查询平台所有订单（不过滤 is_deleted），支持按状态、用户ID、订单号、支付/发货/收货时间范围筛选
     *
     * @param status    订单状态（可选）
     * @param userId    用户ID（可选）
     * @param orderNo   订单号（可选，模糊匹配）
     * @param payTimeStart    支付时间范围-起始（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param payTimeEnd      支付时间范围-结束（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param deliveryTimeStart 发货时间范围-起始（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param deliveryTimeEnd   发货时间范围-结束（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param receiveTimeStart 收货时间范围-起始（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param receiveTimeEnd   收货时间范围-结束（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param page     页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页数据：{ list, page, pageSize, total }
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
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

        Map<String, Object> data = new HashMap<>();
        data.put("list", orders);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 管理员根据订单号查询订单详情（不过滤 is_deleted，可查看所有订单包括已删除的）
     *
     * @param orderNo 订单号
     * @return 订单详情（含订单项和发货记录）
     */
    @GetMapping("/admin/detail/{orderNo}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<OrderVo> getOrderDetailByAdmin(@PathVariable String orderNo) {
        OrderVo orderVo = orderService.getOrderDetailByOrderNoForAdmin(orderNo);
        if (orderVo == null) {
            return Result.error("订单不存在");
        }
        return Result.success(orderVo);
    }

    /**
     * 管理员强制取消订单
     * 可取消任意状态的订单，释放库存并记录运营取消原因
     *
     * @param orderId      订单ID
     * @param cancelReason 取消原因（可选）
     * @return 操作结果
     */
    @PutMapping("/admin/cancel/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
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
     * 仅限待付款订单
     *
     * @param orderId        订单ID
     * @param freightAmount  调整运费（可选）
     * @param discountAmount 调整优惠金额（可选）
     * @param payAmount      调整后实付金额（必填）
     * @return 操作结果
     */
    @PutMapping("/admin/adjust/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
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


    // ==================== 管理员和店铺操作 ====================

    /**
     * 管理员审核通过退款
     * 将退款状态置为"已退款"，支付状态置为"已退款"，回滚库存
     *
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PutMapping("/refund/approve/{orderId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
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
     * 将退款状态置为"已拒绝"，记录拒绝原因、拒绝时间、操作人ID
     *
     * @param orderId      订单ID
     * @param rejectReason 拒绝原因（必填）
     * @return 操作结果
     */
    @PutMapping("/refund/reject/{orderId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> rejectRefund(@PathVariable Long orderId,
                                     @RequestParam String rejectReason) {
        if (rejectReason == null || rejectReason.isEmpty()) {
            return Result.error("拒接退款时请填写原因");
        }
        Long operatorId = getCurrentUserId();
        boolean success = orderService.rejectRefund(orderId, rejectReason, operatorId);
        if (success) {
            return Result.success("退款已拒绝", null);
        }
        return Result.error("拒接失败，订单状态异常");
    }

    // ==================== 用户和管理员操作 ====================

    /**
     * 查询退款进度
     * 用户或管理员均可查询，返回退款申请的审核状态和详细信息
     *
     * @param orderId 订单ID
     * @return 退款进度信息
     */
    @GetMapping("/refund/progress/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<RefundProgressVo> getRefundProgress(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();

        // 校验订单归属：订单所有者或管理员可查看
        OrderVo orderVo = orderService.getOrderById(orderId);
        if (orderVo == null) {
            return Result.error("订单不存在");
        }
        if (!orderVo.getOrder().getUserId().equals(userId) && !isAdminUser()) {
            return Result.error("无权查看此订单");
        }

        RefundProgressVo progress = orderService.getRefundProgress(orderId);
        if (progress == null) {
            return Result.error("订单不存在");
        }
        return Result.success(progress);
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
     * 判断当前用户是否是管理员或商家或店铺管理员
     */
    private boolean isAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().contains("SUPER_ADMIN") ||
                                   a.getAuthority().contains("ADMIN") ||
                                   a.getAuthority().contains("SELLER") ||
                                   a.getAuthority().contains("STORE_ADMIN"));
        }
        return false;
    }
}
