package com.cyh.mallportal.service;

import com.cyh.mallportal.dto.OrderCreateDto;
import com.cyh.mallportal.entity.Order;
import com.cyh.mallportal.entity.OrderItem;
import com.cyh.mallportal.vo.OrderVo;

import java.util.List;

/**
 * 订单服务接口
 * 提供订单业务逻辑操作
 */
public interface OrderService {

    /**
     * 创建订单
     *
     * @param userId      用户ID
     * @param orderCreateDto 订单创建DTO
     * @return 订单VO
     */
    OrderVo createOrder(Long userId, OrderCreateDto orderCreateDto);

    /**
     * 从购物车创建订单（结算）
     *
     * @param userId       用户ID
     * @param addressId    收货地址ID
     * @param payType      支付方式
     * @param buyerMessage 买家留言（可选）
     * @return 订单VO
     */
    OrderVo createOrderFromCart(Long userId, Long addressId, String payType, String buyerMessage);

    /**
     * 根据ID获取订单详情
     *
     * @param id 订单ID
     * @return 订单VO
     */
    OrderVo getOrderById(Long id);

    /**
     * 根据订单号获取订单详情
     *
     * @param orderNo 订单号
     * @return 订单VO
     */
    OrderVo getOrderByOrderNo(String orderNo);

    /**
     * 根据用户ID获取订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> getOrdersByUserId(Long userId);

    /**
     * 根据用户ID和状态获取订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> getOrdersByUserIdAndStatus(Long userId, Integer status);

    /**
     * 取消订单
     *
     * @param orderId      订单ID
     * @param cancelReason 取消原因
     * @return 是否取消成功
     */
    boolean cancelOrder(Long orderId, String cancelReason);

    /**
     * 支付订单
     *
     * @param orderId  订单ID
     * @param payType  支付方式
     * @return 是否支付成功
     */
    boolean payOrder(Long orderId, String payType);

    /**
     * 发货
     *
     * @param orderId         订单ID
     * @param deliveryCompany 物流公司
     * @param deliveryNo      物流单号
     * @return 是否发货成功
     */
    boolean deliverOrder(Long orderId, String deliveryCompany, String deliveryNo);

    /**
     * 确认收货
     *
     * @param orderId 订单ID
     * @return 是否确认成功
     */
    boolean confirmOrder(Long orderId);

    /**
     * 删除订单
     *
     * @param orderId 订单ID
     * @return 是否删除成功
     */
    boolean deleteOrder(Long orderId);

    /**
     * 获取订单明细列表
     *
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    List<OrderItem> getOrderItems(Long orderId);

    /**
     * 获取用户订单数量
     *
     * @param userId 用户ID
     * @return 订单数量
     */
    int countOrders(Long userId);

    /**
     * 获取订单状态描述
     *
     * @param status 订单状态
     * @return 状态描述
     */
    String getStatusDesc(Integer status);

    /**
     * 获取支付状态描述
     *
     * @param payStatus 支付状态
     * @return 支付状态描述
     */
    String getPayStatusDesc(Integer payStatus);

    /**
     * 获取支付方式描述
     *
     * @param payType 支付方式
     * @return 支付方式描述
     */
    String getPayTypeDesc(String payType);

    // ==================== 商家订单查询 ====================

    /**
     * 分页查询商家店铺订单列表
     * 通过订单项(order_items) → SPU(spu) 关联链路获取商家名下所有订单
     *
     * @param sellerId 商家用户ID（对应 spu.seller_id）
     * @param page     页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    List<Order> getOrdersBySellerId(Long sellerId, Integer page, Integer pageSize);

    /**
     * 分页查询商家店铺订单列表（按状态筛选）
     *
     * @param sellerId 商家用户ID
     * @param status   订单状态
     * @param page     页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    List<Order> getOrdersBySellerIdAndStatus(Long sellerId, Integer status, Integer page, Integer pageSize);

    /**
     * 统计商家店铺订单总数
     *
     * @param sellerId 商家用户ID
     * @return 订单总数
     */
    int countOrdersBySellerId(Long sellerId);

    /**
     * 统计商家店铺指定状态订单总数
     *
     * @param sellerId 商家用户ID
     * @param status   订单状态
     * @return 订单总数
     */
    int countOrdersBySellerIdAndStatus(Long sellerId, Integer status);

    /**
     * 商家获取订单详情
     * 校验订单是否属于该商家（通过 order_items → spu.seller_id），不属于时返回 null
     *
     * @param orderNo  订单号
     * @param sellerId 商家用户ID
     * @return 订单详情 VO，不属于该商家时返回 null
     */
    OrderVo getOrderDetailBySellerId(String orderNo, Long sellerId);
}