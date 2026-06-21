package com.cyh.mallportal.service;

import com.cyh.mallportal.dto.OrderCreateDto;
import com.cyh.mallportal.entity.Order;
import com.cyh.mallportal.vo.OrderListItemVo;
import com.cyh.mallportal.vo.OrderStatusCountVo;
import com.cyh.mallportal.vo.OrderVo;
import com.cyh.mallportal.vo.RefundProgressVo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * @return 创建的订单ID
     */
    Long createOrder(Long userId, OrderCreateDto orderCreateDto);

    /**
     * 从购物车创建订单（结算）
     * 每个 SKU 生成一笔独立订单
     *
     * @param userId       用户ID
     * @param addressId    收货地址ID
     * @param buyerMessage 买家留言（可选）
     * @return 创建的订单ID列表
     */
    List<Long> createOrderFromCart(Long userId, Long addressId, String buyerMessage);

    /**
     * 根据ID获取订单详情
     *
     * @param id 订单ID
     * @return 订单VO
     */
    OrderVo getOrderById(Long id);

    /**
     * 根据ID获取订单详情（用户端使用，过滤 is_deleted=1 的已删除订单）
     *
     * @param id 订单ID
     * @return 订单VO，已删除时返回 null
     */
    OrderVo getOrderByIdForUser(Long id);

    /**
     * 根据订单号获取订单详情
     *
     * @param orderNo 订单号
     * @return 订单VO
     */
    OrderVo getOrderByOrderNo(String orderNo);

    /**
     * 分页获取用户订单列表（带商品明细，自动过滤 is_deleted=0）
     *
     * @param userId   用户ID
     * @param status   订单状态（可选，传 null 查询全部）
     * @param page     页码
     * @param pageSize 每页数量
     * @return 订单列表项VO列表
     */
    List<OrderListItemVo> getOrderListWithItems(Long userId, Integer status, Integer page, Integer pageSize);

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
     * 批量付款
     * 对当前用户的多笔待付款订单进行批量支付
     *
     * @param orderIds 订单ID列表
     * @param payType  支付方式（alipay/wechat）
     * @param userId   当前用户ID
     * @return 批量付款结果汇总：{ success: [订单ID...], fail: [{ orderId, reason }...] }
     */
    Map<String, Object> batchPayOrders(List<Long> orderIds, String payType, Long userId);

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
     * 获取用户订单数量
     *
     * @param userId 用户ID
     * @return 订单数量
     */
    int countOrders(Long userId);

    /**
     * 统计用户指定状态订单数量
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单数量
     */
    int countOrders(Long userId, Integer status);

    /**
     * 批量统计用户各状态订单数量
     * 一次性查询待付款、待发货、待收货、退款中的数量
     *
     * @param userId 用户ID
     * @return 各状态订单数量 VO
     */
    OrderStatusCountVo countOrderStatusByUserId(Long userId);

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
     * 商家分页查询店铺订单列表（带商品明细，多条件筛选）
     * 返回 OrderListItemVo，包含订单摘要和商品明细列表
     *
     * @param sellerId  商家用户ID
     * @param status    订单状态（可选）
     * @param userId    用户ID（可选）
     * @param orderNo   订单号（可选，模糊匹配）
     * @param payTimeStart    支付时间范围-起始（可选）
     * @param payTimeEnd      支付时间范围-结束（可选）
     * @param deliveryTimeStart 发货时间范围-起始（可选）
     * @param deliveryTimeEnd   发货时间范围-结束（可选）
     * @param receiveTimeStart 收货时间范围-起始（可选）
     * @param receiveTimeEnd   收货时间范围-结束（可选）
     * @param page     页码
     * @param pageSize 每页数量
     * @return 订单列表项VO列表
     */
    List<OrderListItemVo> getSellerOrderListWithItems(Long sellerId,
                                                      Integer status,
                                                      Long userId,
                                                      String orderNo,
                                                      LocalDateTime payTimeStart,
                                                      LocalDateTime payTimeEnd,
                                                      LocalDateTime deliveryTimeStart,
                                                      LocalDateTime deliveryTimeEnd,
                                                      LocalDateTime receiveTimeStart,
                                                      LocalDateTime receiveTimeEnd,
                                                      Integer page, Integer pageSize);

    /**
     * 统计商家店铺订单总数（多条件筛选）
     * 与 getSellerOrderListWithItems 条件完全一致，用于分页总记录数
     */
    int countSellerOrdersByFilters(Long sellerId,
                                   Integer status,
                                   Long userId,
                                   String orderNo,
                                   LocalDateTime payTimeStart,
                                   LocalDateTime payTimeEnd,
                                   LocalDateTime deliveryTimeStart,
                                   LocalDateTime deliveryTimeEnd,
                                   LocalDateTime receiveTimeStart,
                                   LocalDateTime receiveTimeEnd);

    /**
     * 商家获取订单详情
     * 校验订单是否属于该商家（通过 order_items → spu.seller_id），不属于时返回 null
     *
     * @param orderNo  订单号
     * @param sellerId 商家用户ID
     * @return 订单详情 VO，不属于该商家时返回 null
     */
    OrderVo getOrderDetailBySellerId(String orderNo, Long sellerId);

    // ==================== 管理员订单查询（不过滤 is_deleted） ====================

    /**
     * 管理员根据订单号查询订单详情（不过滤 is_deleted，可查看所有订单包括已删除的）
     *
     * @param orderNo 订单号
     * @return 订单详情 VO
     */
    OrderVo getOrderDetailByOrderNoForAdmin(String orderNo);

    // ==================== 全局订单查询（运营管理员/超级管理员） ====================

    /**
     * 分页查询全部订单（运营管理员/超级管理员使用，多条件筛选）
     * 支持按状态、用户ID、订单号、支付/发货/收货时间范围筛选
     *
     * @param status    订单状态（可选，null 表示全部）
     * @param userId    用户ID（可选）
     * @param orderNo   订单号（可选，模糊匹配）
     * @param payTimeStart    支付时间范围-起始（可选）
     * @param payTimeEnd      支付时间范围-结束（可选）
     * @param deliveryTimeStart 发货时间范围-起始（可选）
     * @param deliveryTimeEnd   发货时间范围-结束（可选）
     * @param receiveTimeStart 收货时间范围-起始（可选）
     * @param receiveTimeEnd   收货时间范围-结束（可选）
     * @param page     页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    List<Order> getAllOrders(Integer status,
                             Long userId,
                             String orderNo,
                             LocalDateTime payTimeStart,
                             LocalDateTime payTimeEnd,
                             LocalDateTime deliveryTimeStart,
                             LocalDateTime deliveryTimeEnd,
                             LocalDateTime receiveTimeStart,
                             LocalDateTime receiveTimeEnd,
                             Integer page, Integer pageSize);

    /**
     * 统计全部订单总数（多条件筛选）
     * 与 getAllOrders 条件完全一致，用于分页总记录数
     *
     * @param status    订单状态（可选，null 表示全部）
     * @param userId    用户ID（可选）
     * @param orderNo   订单号（可选，模糊匹配）
     * @param payTimeStart    支付时间范围-起始（可选）
     * @param payTimeEnd      支付时间范围-结束（可选）
     * @param deliveryTimeStart 发货时间范围-起始（可选）
     * @param deliveryTimeEnd   发货时间范围-结束（可选）
     * @param receiveTimeStart 收货时间范围-起始（可选）
     * @param receiveTimeEnd   收货时间范围-结束（可选）
     * @return 订单总数
     */
    int countAllOrders(Integer status,
                       Long userId,
                       String orderNo,
                       LocalDateTime payTimeStart,
                       LocalDateTime payTimeEnd,
                       LocalDateTime deliveryTimeStart,
                       LocalDateTime deliveryTimeEnd,
                       LocalDateTime receiveTimeStart,
                       LocalDateTime receiveTimeEnd);

    // ==================== 运营管理员操作 ====================

    /**
     * 管理员强制取消订单
     * 可取消任意状态的订单，释放库存并记录取消原因
     *
     * @param orderId      订单ID
     * @param cancelReason 取消原因（运营操作）
     * @return 是否取消成功
     */
    boolean adminCancelOrder(Long orderId, String cancelReason);

    /**
     * 管理员调整订单金额
     * 仅限待付款订单（status=1）
     *
     * @param orderId        订单ID
     * @param freightAmount  调整后的运费（可选，null 不修改）
     * @param discountAmount 调整后的优惠金额（可选，null 不修改）
     * @param payAmount      调整后的实付金额（必填）
     * @return 是否调整成功
     */
    boolean adjustOrderAmount(Long orderId, BigDecimal freightAmount, BigDecimal discountAmount, BigDecimal payAmount);

    /**
     * 用户申请退款
     * 仅限已支付订单（payStatus=1）
     *
     * @param orderId      订单ID
     * @param refundReason 退款原因
     * @return 是否申请成功
     */
    boolean applyRefund(Long orderId, String refundReason);

    /**
     * 用户取消退款申请
     * 仅允许 status=6（退款中）的订单取消，取消后回到退款前的状态
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 是否取消成功
     */
    boolean cancelRefund(Long orderId, Long userId);

    /**
     * 管理员审核通过退款
     * 将退款状态置为"已退款"，记录操作人ID
     *
     * @param orderId 订单ID
     * @param operatorId 操作人ID
     * @return 是否操作成功
     */
    boolean approveRefund(Long orderId, Long operatorId);

    /**
     * 管理员拒接退款
     * 将退款状态置为"已拒绝"，记录拒绝原因、拒绝时间、操作人ID
     *
     * @param orderId      订单ID
     * @param rejectReason 拒绝原因
     * @param operatorId   操作人ID
     * @return 是否操作成功
     */
    boolean rejectRefund(Long orderId, String rejectReason, Long operatorId);

    /**
     * 查询退款进度
     * 根据订单当前状态返回退款进度信息
     *
     * @param orderId 订单ID
     * @return 退款进度VO
     */
    RefundProgressVo getRefundProgress(Long orderId);
}