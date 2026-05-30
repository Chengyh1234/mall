package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.dto.OrderCreateDto;
import com.cyh.mallportal.entity.Order;
import com.cyh.mallportal.entity.OrderItem;
import com.cyh.mallportal.entity.User;
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
     * @return 订单信息
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public Result<OrderVo> createOrder(@RequestBody OrderCreateDto orderCreateDto) {
        Long userId = getCurrentUserId();

        if (orderCreateDto.getItems() == null || orderCreateDto.getItems().isEmpty()) {
            return Result.error("订单商品不能为空");
        }

        if (orderCreateDto.getAddressId() == null && (orderCreateDto.getReceiverName() == null || orderCreateDto.getReceiverPhone() == null)) {
            return Result.error("收货信息不能为空");
        }

        try {
            OrderVo orderVo = orderService.createOrder(userId, orderCreateDto);
            return Result.success("订单创建成功", orderVo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从购物车结算创建订单
     * 使用购物车中已选中的商品创建订单
     *
     * @param addressId    收货地址ID
     * @param payType      支付方式（alipay/wechat）
     * @param buyerMessage 买家留言（可选）
     * @return 订单信息
     */
    @PostMapping("/create-from-cart")
    @PreAuthorize("isAuthenticated()")
    public Result<OrderVo> createOrderFromCart(@RequestParam Long addressId,
                                               @RequestParam(defaultValue = "wechat") String payType,
                                               @RequestParam(required = false) String buyerMessage) {
        Long userId = getCurrentUserId();

        if (addressId == null) {
            return Result.error("收货地址不能为空");
        }

        try {
            OrderVo orderVo = orderService.createOrderFromCart(userId, addressId, payType, buyerMessage);
            if (orderVo != null) {
                return Result.success("订单创建成功", orderVo);
            }
            return Result.error("购物车中没有选中的商品");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据订单号获取订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/detail/{orderNo}")
    @PreAuthorize("isAuthenticated()")
    public Result<OrderVo> getOrderByOrderNo(@PathVariable String orderNo) {
        OrderVo orderVo = orderService.getOrderByOrderNo(orderNo);
        if (orderVo != null) {
            Long userId = getCurrentUserId();
            if (!orderVo.getOrder().getUserId().equals(userId) && !isAdminUser()) {
                return Result.error("无权查看此订单");
            }
            return Result.success(orderVo);
        }
        return Result.error("订单不存在");
    }

    /**
     * 获取当前用户的订单列表
     *
     * @param status 订单状态（可选）
     * @return 订单列表
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Order>> getOrdersByUserId(@RequestParam(required = false) Integer status) {
        Long userId = getCurrentUserId();

        List<Order> orders;
        if (status != null) {
            orders = orderService.getOrdersByUserIdAndStatus(userId, status);
        } else {
            orders = orderService.getOrdersByUserId(userId);
        }

        return Result.success(orders);
    }

    /**
     * 商家分页查询店铺订单列表
     * 根据当前登录的商家用户ID，通过 order_items → spu 关联链路查询店铺订单
     *
     * @param status   订单状态（可选，用于筛选）
     * @param page     页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页数据：{ list, page, pageSize, total }
     */
    @GetMapping("/seller/list")
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> getOrdersBySellerId(@RequestParam(required = false) Integer status,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        Long sellerId = getCurrentUserId();

        List<Order> orders;
        int total;
        if (status != null) {
            orders = orderService.getOrdersBySellerIdAndStatus(sellerId, status, page, pageSize);
            total = orderService.countOrdersBySellerIdAndStatus(sellerId, status);
        } else {
            orders = orderService.getOrdersBySellerId(sellerId, page, pageSize);
            total = orderService.countOrdersBySellerId(sellerId);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", orders);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 商家查询订单详情，不是分页查询
     * 校验订单是否属于当前商家（通过 order_items → spu.seller_id），仅返回本店铺的订单
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
     * 获取订单明细列表
     *
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    @GetMapping("/items/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<OrderItem>> getOrderItems(@PathVariable Long orderId) {
        OrderVo orderVo = orderService.getOrderById(orderId);
        if (orderVo == null) {
            return Result.error("订单不存在");
        }

        Long userId = getCurrentUserId();
        if (!orderVo.getOrder().getUserId().equals(userId) && !isAdminUser()) {
            return Result.error("无权查看此订单");
        }

        List<OrderItem> items = orderService.getOrderItems(orderId);
        return Result.success(items);
    }

    /**
     * 取消订单
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

        OrderVo orderVo = orderService.getOrderById(orderId);
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
     * 支付订单
     *
     * @param orderId 订单ID
     * @param payType 支付方式（alipay/wechat）
     * @return 支付结果
     */
    @PutMapping("/pay/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> payOrder(@PathVariable Long orderId,
                                 @RequestParam(defaultValue = "alipay") String payType) {
        Long userId = getCurrentUserId();

        OrderVo orderVo = orderService.getOrderById(orderId);
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
     * 发货（管理员操作）
     *
     * @param orderId         订单ID
     * @param deliveryCompany 物流公司
     * @param deliveryNo      物流单号
     * @return 发货结果
     */
    @PutMapping("/deliver/{orderId}")
    @PreAuthorize("hasAuthority('order:deliver') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> deliverOrder(@PathVariable Long orderId,
                                     @RequestParam String deliveryCompany,
                                     @RequestParam String deliveryNo) {
        boolean success = orderService.deliverOrder(orderId, deliveryCompany, deliveryNo);
        if (success) {
            return Result.success("发货成功", null);
        }
        return Result.error("发货失败，订单状态不允许");
    }

    /**
     * 确认收货
     *
     * @param orderId 订单ID
     * @return 确认结果
     */
    @PutMapping("/confirm/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> confirmOrder(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();

        OrderVo orderVo = orderService.getOrderById(orderId);
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
     * 删除订单
     *
     * @param orderId 订单ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteOrder(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();

        OrderVo orderVo = orderService.getOrderById(orderId);
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
     * 获取订单状态描述（公开接口）
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
