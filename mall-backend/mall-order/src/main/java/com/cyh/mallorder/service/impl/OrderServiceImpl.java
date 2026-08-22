package com.cyh.mallorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallcommon.dto.AddressDto;
import com.cyh.mallcommon.dto.SkuInfoDto;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallorder.dto.OrderCreateDto;
import com.cyh.mallorder.dto.OrderDeliveryDto;
import com.cyh.mallorder.dto.OrderItemDto;
import com.cyh.mallorder.entity.CartItem;
import com.cyh.mallorder.entity.Order;
import com.cyh.mallorder.entity.OrderDelivery;
import com.cyh.mallorder.entity.OrderItem;
import com.cyh.mallorder.feign.AddressClient;
import com.cyh.mallorder.mapper.OrderDeliveryMapper;
import com.cyh.mallorder.mapper.OrderItemMapper;
import com.cyh.mallorder.mapper.OrderMapper;
import com.cyh.mallorder.mq.event.OrderExpireEvent;
import com.cyh.mallorder.mq.publisher.OrderEventPublisher;
import com.cyh.mallorder.service.CartItemService;
import com.cyh.mallorder.service.OrderDeliveryService;
import com.cyh.mallorder.service.OrderService;
import com.cyh.mallorder.vo.OrderDatailVo;
import com.cyh.mallorder.vo.OrderListItemVo;
import com.cyh.mallorder.vo.OrderStatusCountVo;
import com.cyh.mallorder.vo.RefundProgressVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 * 提供订单业务逻辑的具体实现
 * 库存采用 Redis + Lua 脚本预扣模式：下单冻结 → 支付实扣 → 超时/取消释放
 * 跨模块数据通过 Feign 远程调用获取
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderDeliveryMapper orderDeliveryMapper;
    private final OrderDeliveryService orderDeliveryService;
    private final CartItemService cartItemService;
    private final StockLuaScript stockLuaScript;
    private final OrderEventPublisher orderEventPublisher;
    private final AddressClient addressClient;

    @Value("${mall.order.pay-expire-minutes:30}")
    private int payExpireMinutes;

    /**
     * 创建订单（直接下单）
     * <p>
     * 核心流程：生成订单号 → 通过 Feign 获取收货地址 → Redis Lua 冻结库存（库存不足时回滚已冻结的 SKU）
     * → 落库订单主表 + 明细表 → 事务提交后异步发送库存同步消息和订单超时消息。
     * 库存采用预扣模式：冻结成功才创建订单，避免超卖。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long userId, OrderCreateDto orderCreateDto) {
        log.info("创建订单, 用户ID: {}", userId);

        String orderNo = generateOrderNo();

        // 通过 Feign 获取收货地址
        AddressDto address = null;
        if (orderCreateDto.getAddressId() != null) {
            Result<AddressDto> addressResult = addressClient.getAddressDetail(orderCreateDto.getAddressId());
            if (addressResult != null) {
                address = addressResult.getData();
            }
        }

        // 冻结库存
        List<Long> frozenSkuIds = new ArrayList<>();
        for (OrderItemDto itemDto : orderCreateDto.getItems()) {
            boolean success = stockLuaScript.freezeStock(itemDto.getSkuId(), itemDto.getQuantity());
            if (!success) {
                rollbackFrozen(frozenSkuIds, orderCreateDto.getItems());
                throw new BusinessException("库存不足, SKU: " + itemDto.getSkuId());
            }
            frozenSkuIds.add(itemDto.getSkuId());
        }

        // 创建订单实体
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(orderCreateDto.getTotalAmount() != null ? orderCreateDto.getTotalAmount() : BigDecimal.ZERO);
        order.setDiscountAmount(orderCreateDto.getDiscountAmount() != null ? orderCreateDto.getDiscountAmount() : BigDecimal.ZERO);
        order.setFreightAmount(orderCreateDto.getFreightAmount() != null ? orderCreateDto.getFreightAmount() : BigDecimal.ZERO);
        order.setPayAmount(orderCreateDto.getPayAmount() != null ? orderCreateDto.getPayAmount() : BigDecimal.ZERO);
        order.setStatus(1);
        order.setPayStatus(0);
        order.setRemark(orderCreateDto.getRemark());
        order.setExpireTime(LocalDateTime.now().plusMinutes(payExpireMinutes));

        if (address != null) {
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getReceiverPhone());
            order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        } else {
            order.setReceiverName(orderCreateDto.getReceiverName());
            order.setReceiverPhone(orderCreateDto.getReceiverPhone());
            order.setReceiverAddress(orderCreateDto.getReceiverAddress());
        }

        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);
        log.info("订单创建成功, 订单ID: {}, 订单号: {}, 支付截止: {}", order.getId(), orderNo, order.getExpireTime());

        // 创建订单明细
        List<Long> skuIds = new ArrayList<>();
        for (OrderItemDto itemDto : orderCreateDto.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderNo(orderNo);
            orderItem.setSkuId(itemDto.getSkuId());
            orderItem.setSpuId(itemDto.getSpuId());
            orderItem.setProductName(itemDto.getProductName() != null ? itemDto.getProductName() : "");
            orderItem.setProductImage(itemDto.getProductImage() != null ? itemDto.getProductImage() : "");
            orderItem.setSkuSpecs(itemDto.getSkuSpecs());
            orderItem.setPrice(itemDto.getPrice() != null ? itemDto.getPrice() : BigDecimal.ZERO);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setTotalAmount(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
            orderItem.setGiftFlag(itemDto.getGiftFlag() != null ? itemDto.getGiftFlag() : 0);
            orderItem.setCreatedAt(LocalDateTime.now());

            orderItemMapper.insert(orderItem);
            skuIds.add(itemDto.getSkuId());
        }

        // 事务提交后，异步处理
        Long finalOrderId = order.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderEventPublisher.publishStockSync(skuIds);
                orderEventPublisher.publishOrderExpire(
                        new OrderExpireEvent()
                                .setOrderId(finalOrderId)
                                .setOrderNo(orderNo)
                );
            }
        });
        log.info("订单明细创建完成, 订单ID: {}", order.getId());
        return order.getId();
    }

    /**
     * 从购物车创建订单
     * <p>
     * 遍历购物车中已选中的商品，依次执行：冻结库存 → 创建订单主表 + 明细表 → 清空购物车选中项。
     * 每个 SKU 生成独立订单（一单一品），事务提交后异步发送库存同步和订单超时消息。
     * 任一 SKU 库存不足时回滚本批次所有已冻结的库存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createOrderFromCart(Long userId, Long addressId, String buyerMessage) {
        log.info("从购物车创建订单, 用户ID: {}, 地址ID: {}", userId, addressId);

        List<CartItem> selectedItems = cartItemService.getSelectedItems(userId);
        if (selectedItems == null || selectedItems.isEmpty()) {
            log.warn("购物车中没有选中的商品");
            return Collections.emptyList();
        }

        // 通过 Feign 获取收货地址
        Result<AddressDto> addressResult = addressClient.getAddressDetail(addressId);
        if (addressResult == null || addressResult.getData() == null) {
            log.warn("收货地址不存在, {}", addressId);
            return Collections.emptyList();
        }
        AddressDto address = addressResult.getData();

        // 冻结库存
        List<Long> frozenSkuIds = new ArrayList<>();
        for (CartItem item : selectedItems) {
            boolean success = stockLuaScript.freezeStock(item.getSkuId(), item.getQuantity());
            if (!success) {
                rollbackFrozenByCart(frozenSkuIds, selectedItems);
                throw new BusinessException("商品[" + item.getProductName() + "]库存不足");
            }
            frozenSkuIds.add(item.getSkuId());
        }

        List<Long> resultList = new ArrayList<>();
        List<Long> skuIds = new ArrayList<>();
        Map<Long, String> orderIdToNo = new HashMap<>();

        for (CartItem item : selectedItems) {
            String orderNo = generateOrderNo();

            Order order = new Order();
            BigDecimal subtotal = item.getPrice() != null && item.getQuantity() != null
                    ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                    : BigDecimal.ZERO;
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setTotalAmount(subtotal);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setFreightAmount(BigDecimal.ZERO);
            order.setPayAmount(subtotal);
            order.setStatus(1);
            order.setPayStatus(0);
            order.setRemark(buyerMessage);
            order.setExpireTime(LocalDateTime.now().plusMinutes(payExpireMinutes));

            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getReceiverPhone());
            order.setReceiverAddress(address.getProvince() + address.getCity()
                    + address.getDistrict() + address.getDetailAddress());

            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.insert(order);
            log.info("订单创建成功, 订单ID: {}, 订单号: {}, 金额: {}, 支付截止: {}",
                    order.getId(), orderNo, order.getPayAmount(), order.getExpireTime());

            // 创建订单明细
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderNo(orderNo);
            orderItem.setSkuId(item.getSkuId());
            orderItem.setSpuId(item.getSpuId());
            orderItem.setProductName(item.getProductName() != null ? item.getProductName() : "");
            orderItem.setProductImage(item.getProductImage() != null ? item.getProductImage() : "");
            orderItem.setSkuSpecs(item.getSkuSpecs());
            orderItem.setPrice(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setTotalAmount(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
            orderItem.setGiftFlag(0);
            orderItem.setCreatedAt(LocalDateTime.now());

            orderItemMapper.insert(orderItem);
            skuIds.add(item.getSkuId());

            resultList.add(order.getId());
            orderIdToNo.put(order.getId(), orderNo);
        }

        cartItemService.clearSelected(userId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderEventPublisher.publishStockSync(skuIds);
                for (Map.Entry<Long, String> entry : orderIdToNo.entrySet()) {
                    orderEventPublisher.publishOrderExpire(
                            new OrderExpireEvent()
                                    .setOrderId(entry.getKey())
                                    .setOrderNo(entry.getValue())
                    );
                }
            }
        });

        log.info("从购物车创建订单完成, 用户ID: {}, 共 {} 笔订单", userId, resultList.size());
        return resultList;
    }

    private void rollbackFrozen(List<Long> frozenSkuIds, List<OrderItemDto> items) {
        for (OrderItemDto itemDto : items) {
            if (frozenSkuIds.contains(itemDto.getSkuId())) {
                stockLuaScript.releaseStock(itemDto.getSkuId(), itemDto.getQuantity());
            }
        }
    }

    private void rollbackFrozenByCart(List<Long> frozenSkuIds, List<CartItem> items) {
        for (CartItem item : items) {
            if (frozenSkuIds.contains(item.getSkuId())) {
                stockLuaScript.releaseStock(item.getSkuId(), item.getQuantity());
            }
        }
    }

    /**
     * 根据订单 ID 查询订单详情（含商品明细和发货记录）
     * 组装 OrderDatailVo，包括商品列表、发货记录、状态描述等。
     */
    @Override
    public OrderDatailVo getOrderById(Long id) {
        log.info("获取订单详情, 订单ID: {}", id);

        Order order = orderMapper.selectById(id);
        if (order == null) {
            return null;
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(id);
        List<OrderDelivery> deliveries = orderDeliveryMapper.selectByOrderId(id);

        List<OrderDatailVo.ItemVo> itemVos = items.stream().map(item -> {
            OrderDatailVo.ItemVo itemVo = new OrderDatailVo.ItemVo();
            itemVo.setProductName(item.getProductName());
            itemVo.setProductImage(item.getProductImage());
            itemVo.setSkuSpecs(item.getSkuSpecs());
            itemVo.setPrice(item.getPrice());
            itemVo.setQuantity(item.getQuantity());
            itemVo.setTotalAmount(item.getTotalAmount());
            return itemVo;
        }).collect(Collectors.toList());

        List<OrderDatailVo.DeliveryVo> deliveryVos = deliveries.stream().map(d -> {
            OrderDatailVo.DeliveryVo deliveryVo = new OrderDatailVo.DeliveryVo();
            deliveryVo.setDeliveryCompany(d.getDeliveryCompany());
            deliveryVo.setDeliveryNo(d.getDeliveryNo());
            deliveryVo.setDeliveryStatus(d.getDeliveryStatus());
            deliveryVo.setDeliveryTime(d.getDeliveryTime());
            return deliveryVo;
        }).collect(Collectors.toList());

        OrderDatailVo vo = new OrderDatailVo();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setStatus(order.getStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setPayTime(order.getPayTime());
        vo.setExpireTime(order.getExpireTime());
        vo.setReceiveTime(order.getReceiveTime());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setCancelReason(order.getCancelReason());
        vo.setRefundReason(order.getRefundReason());
        vo.setRefundAmount(order.getRefundAmount());
        vo.setRejectReason(order.getRejectReason());
        vo.setRejectedAt(order.getRejectedAt());
        vo.setUserId(order.getUserId());
        vo.setStatusDesc(getStatusDesc(order.getStatus()));
        vo.setPayStatusDesc(getPayStatusDesc(order.getPayStatus()));
        vo.setPayTypeDesc(getPayTypeDesc(order.getPayType()));
        vo.setItems(itemVos);
        vo.setDeliveries(deliveryVos);

        return vo;
    }

    /**
     * 根据订单 ID 查询订单详情（用户侧，排除已删除的订单）
     */
    @Override
    public OrderDatailVo getOrderByIdForUser(Long id) {
        log.info("获取订单详情（用户过滤）, 订单ID: {}", id);

        Order order = orderMapper.selectById(id);
        if (order == null || order.getIsDeleted() == 1) {
            log.warn("订单不存在或已删除, 订单ID: {}", id);
            return null;
        }

        return getOrderById(id);
    }

    /**
     * 根据订单号查询订单详情
     */
    @Override
    public OrderDatailVo getOrderByOrderNo(String orderNo) {
        log.info("获取订单详情, 订单号: {}", orderNo);

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return null;
        }

        return getOrderById(order.getId());
    }

    /**
     * 分页查询用户订单列表（带商品明细）
     * 支持按订单状态筛选，一次查询出所有商品明细后按订单 ID 分组组装。
     */
    @Override
    public List<OrderListItemVo> getOrderListWithItems(Long userId, Integer status, Integer page, Integer pageSize) {
        log.info("分页获取用户订单列表（带商品明细）, 用户ID: {}, 状态: {}, 页码: {}, 每页: {}", userId, status, page, pageSize);

        Page<Order> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        IPage<Order> orderPage;
        if (status != null) {
            orderPage = orderMapper.selectByUserIdAndStatusPaged(pageParam, userId, status);
        } else {
            orderPage = orderMapper.selectByUserIdPaged(pageParam, userId);
        }
        List<Order> orders = orderPage.getRecords();

        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        List<OrderItem> allItems = orderItemMapper.selectByOrderIds(orderIds);

        Map<Long, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        return orders.stream().map(order -> {
            List<OrderItem> items = itemMap.getOrDefault(order.getId(), Collections.emptyList());

            List<OrderListItemVo.Item> itemVos = items.stream().map(item -> {
                OrderListItemVo.Item itemVo = new OrderListItemVo.Item();
                itemVo.setProductName(item.getProductName());
                itemVo.setProductImage(item.getProductImage());
                itemVo.setSkuSpecs(item.getSkuSpecs());
                itemVo.setPrice(item.getPrice());
                itemVo.setQuantity(item.getQuantity());
                itemVo.setTotalAmount(item.getTotalAmount());
                return itemVo;
            }).collect(Collectors.toList());

            OrderListItemVo vo = new OrderListItemVo();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setUserId(order.getUserId());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setPayAmount(order.getPayAmount());
            vo.setDiscountAmount(order.getDiscountAmount());
            vo.setFreightAmount(order.getFreightAmount());
            vo.setPayTime(order.getPayTime());
            vo.setExpireTime(order.getExpireTime());
            vo.setStatus(order.getStatus());
            vo.setRefundAmount(order.getRefundAmount());
            vo.setRejectReason(order.getRejectReason());
            vo.setRejectedAt(order.getRejectedAt());
            vo.setItems(itemVos);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 取消订单（用户侧）
     * <p>
     * 仅待付款状态的订单可取消。取消时释放 Redis 中已冻结的库存，
     * 更新订单状态为"已取消"，事务提交后异步同步库存到 MySQL。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, String cancelReason) {
        log.info("取消订单, 订单ID: {}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            log.warn("订单不存在或已删除, {}", orderId);
            return false;
        }

        if (order.getStatus() != 1) {
            log.warn("订单状态不允许取消, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        List<Long> skuIds = new ArrayList<>();
        for (OrderItem item : items) {
            stockLuaScript.releaseStock(item.getSkuId(), item.getQuantity());
            skuIds.add(item.getSkuId());
        }

        order.setStatus(5);
        order.setCancelReason(cancelReason);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单取消成功, 订单ID: {}", orderId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderEventPublisher.publishStockSync(skuIds);
            }
        });
        return true;
    }

    /**
     * 支付订单
     * <p>
     * 仅待付款且未超时的订单可支付。支付时将 Redis 中冻结库存转为确认扣减，
     * 更新订单状态为"待发货"，事务提交后异步同步库存到 MySQL。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId, String payType) {
        log.info("支付订单, 订单ID: {}, 支付方式: {}", orderId, payType);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            log.warn("订单不存在或已删除, {}", orderId);
            return false;
        }

        if (order.getStatus() != 1) {
            log.warn("订单状态不允许支付, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
            log.warn("订单已超时, 订单ID: {}", orderId);
            return false;
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        List<Long> skuIds = new ArrayList<>();
        for (OrderItem item : items) {
            stockLuaScript.confirmStock(item.getSkuId(), item.getQuantity());
            skuIds.add(item.getSkuId());
        }

        order.setPayStatus(1);
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        order.setStatus(2);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单支付成功, 订单ID: {}", orderId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderEventPublisher.publishStockSync(skuIds);
            }
        });
        return true;
    }

    /**
     * 批量支付订单
     * <p>
     * 逐笔校验订单状态和超时时间，通过的订单执行 Redis 确认扣减库存并更新状态。
     * 所有失败订单记录原因，最终返回成功/失败汇总列表。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchPayOrders(List<Long> orderIds, String payType, Long userId) {
        log.info("批量付款, 用户ID: {}, 订单数: {}, 支付方式: {}", userId, orderIds.size(), payType);

        List<Long> successIds = new ArrayList<>();
        List<Map<String, Object>> failList = new ArrayList<>();
        List<Long> allSkuIds = new ArrayList<>();

        List<Order> orders = orderMapper.selectByIdsAndUserId(orderIds, userId);
        if (orders.isEmpty()) {
            throw new BusinessException("未找到可支付的订单");
        }

        Map<Long, Order> orderMap = orders.stream()
                .collect(Collectors.toMap(Order::getId, o -> o));

        for (Long id : orderIds) {
            if (!orderMap.containsKey(id)) {
                Map<String, Object> failItem = new HashMap<>();
                failItem.put("orderId", id);
                failItem.put("reason", "订单不存在或不属于当前用户");
                failList.add(failItem);
            }
        }

        for (Order order : orders) {
            Long orderId = order.getId();
            Map<String, Object> failItem = new HashMap<>();
            failItem.put("orderId", orderId);

            if (order.getStatus() != 1) {
                failItem.put("reason", "订单状态不允许支付（当前状态: " + order.getStatus() + "）");
                failList.add(failItem);
                continue;
            }

            if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
                failItem.put("reason", "订单已超时，无法支付");
                failList.add(failItem);
                continue;
            }

            List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
            for (OrderItem item : items) {
                stockLuaScript.confirmStock(item.getSkuId(), item.getQuantity());
                allSkuIds.add(item.getSkuId());
            }

            order.setPayStatus(1);
            order.setPayType(payType);
            order.setPayTime(LocalDateTime.now());
            order.setStatus(2);
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(order);

            successIds.add(orderId);
            log.info("批量付款 - 订单支付成功, 订单ID: {}", orderId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", successIds);
        result.put("fail", failList);
        result.put("totalCount", orderIds.size());
        result.put("successCount", successIds.size());
        result.put("failCount", failList.size());

        log.info("批量付款完成, 成功: {}, 失败: {}", successIds.size(), failList.size());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderEventPublisher.publishStockSync(allSkuIds);
            }
        });
        return result;
    }

    /**
     * 发货
     * <p>
     * 仅待发货状态的订单可发货。更新订单主表物流信息，同时创建发货记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deliverOrder(Long orderId, String deliveryCompany, String deliveryNo) {
        log.info("发货, 订单ID: {}, 物流公司: {}, 物流单号: {}", orderId, deliveryCompany, deliveryNo);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在, {}", orderId);
            return false;
        }

        if (order.getStatus() != 2) {
            log.warn("订单状态不允许发货, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        order.setStatus(3);
        order.setDeliveryCompany(deliveryCompany);
        order.setDeliveryNo(deliveryNo);
        order.setDeliveryTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        OrderDeliveryDto deliveryDto = new OrderDeliveryDto();
        deliveryDto.setOrderId(orderId);
        deliveryDto.setDeliveryCompany(deliveryCompany);
        deliveryDto.setDeliveryNo(deliveryNo);
        deliveryDto.setReceiverName(order.getReceiverName());
        deliveryDto.setReceiverPhone(order.getReceiverPhone());
        orderDeliveryService.createDelivery(deliveryDto);

        log.info("订单发货成功, 订单ID: {}", orderId);
        return true;
    }

    /**
     * 确认收货
     * <p>
     * 仅待收货状态的订单可确认收货。更新订单状态为"已完成"，
     * 事务提交后异步发送销量增加消息。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOrder(Long orderId) {
        log.info("确认收货, 订单ID: {}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            log.warn("订单不存在或已删除, {}", orderId);
            return false;
        }

        if (order.getStatus() != 3) {
            log.warn("订单状态不允许确认收货, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        order.setStatus(4);
        order.setReceiveTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderEventPublisher.publishSalesIncrease(orderId);
            }
        });

        log.info("订单确认收货成功, 订单ID: {}", orderId);
        return true;
    }

    /**
     * 逻辑删除订单（用户侧）
     * 仅已完成或已取消的订单可删除，标记 is_deleted = 1。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long orderId) {
        log.info("逻辑删除订单, 订单ID: {}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            log.warn("订单不存在或已删除, {}", orderId);
            return false;
        }

        if (order.getStatus() != 4 && order.getStatus() != 5) {
            log.warn("订单状态不允许删除, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        order.setIsDeleted(1);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单逻辑删除成功, 订单ID: {}", orderId);
        return true;
    }

    /**
     * 统计用户订单总数
     */
    @Override
    public int countOrders(Long userId) {
        return orderMapper.countByUserId(userId);
    }

    /**
     * 统计用户指定状态的订单数量
     */
    @Override
    public int countOrders(Long userId, Integer status) {
        return orderMapper.countByUserIdAndStatus(userId, status);
    }

    /**
     * 批量统计用户各状态订单数量
     * 一次查询返回待付款、待发货、待收货等各状态的计数。
     */
    @Override
    public OrderStatusCountVo countOrderStatusByUserId(Long userId) {
        log.info("批量统计用户各状态订单数量, 用户ID: {}", userId);
        return orderMapper.countOrderStatusByUserId(userId);
    }

    /**
     * 分页查询商家订单列表
     */
    @Override
    public List<Order> getOrdersBySellerId(Long sellerId, Integer page, Integer pageSize) {
        log.info("分页获取商家订单列表, 商家ID: {}, 页码: {}, 每页: {}", sellerId, page, pageSize);
        Page<Order> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        IPage<Order> result = orderMapper.selectBySellerId(pageParam, sellerId);
        return result.getRecords();
    }

    /**
     * 分页查询商家指定状态的订单列表
     */
    @Override
    public List<Order> getOrdersBySellerIdAndStatus(Long sellerId, Integer status, Integer page, Integer pageSize) {
        log.info("分页获取商家订单列表, 商家ID: {}, 状态: {}, 页码: {}, 每页: {}", sellerId, status, page, pageSize);
        Page<Order> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        IPage<Order> result = orderMapper.selectBySellerIdAndStatus(pageParam, sellerId, status);
        return result.getRecords();
    }

    /**
     * 统计商家订单总数
     */
    @Override
    public int countOrdersBySellerId(Long sellerId) {
        return orderMapper.countBySellerId(sellerId);
    }

    /**
     * 统计商家指定状态的订单数量
     */
    @Override
    public int countOrdersBySellerIdAndStatus(Long sellerId, Integer status) {
        return orderMapper.countBySellerIdAndStatus(sellerId, status);
    }

    /**
     * 商家分页查询店铺订单（带商品明细，支持多条件筛选）
     * <p>
     * 支持按订单状态、用户 ID、订单号、支付/发货/收货时间范围等条件组合查询，
     * 一次查询出所有商品明细后按订单 ID 分组组装。
     */
    @Override
    public List<OrderListItemVo> getSellerOrderListWithItems(Long sellerId,
                                                             Integer status,
                                                             Long userId,
                                                             String orderNo,
                                                             LocalDateTime payTimeStart,
                                                             LocalDateTime payTimeEnd,
                                                             LocalDateTime deliveryTimeStart,
                                                             LocalDateTime deliveryTimeEnd,
                                                             LocalDateTime receiveTimeStart,
                                                             LocalDateTime receiveTimeEnd,
                                                             Integer page, Integer pageSize) {
        log.info("商家分页查询店铺订单（带明细）, sellerId: {}, status: {}, userId: {}, page: {}",
                sellerId, status, userId, page);

        Page<Order> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        IPage<Order> orderPage = orderMapper.selectBySellerIdWithFilters(
                pageParam, sellerId, status, userId, orderNo,
                payTimeStart, payTimeEnd, deliveryTimeStart, deliveryTimeEnd,
                receiveTimeStart, receiveTimeEnd);
        List<Order> orders = orderPage.getRecords();

        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        List<OrderItem> allItems = orderItemMapper.selectByOrderIds(orderIds);

        Map<Long, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        return orders.stream().map(order -> {
            List<OrderItem> items = itemMap.getOrDefault(order.getId(), Collections.emptyList());

            List<OrderListItemVo.Item> itemVos = items.stream().map(item -> {
                OrderListItemVo.Item itemVo = new OrderListItemVo.Item();
                itemVo.setProductName(item.getProductName());
                itemVo.setProductImage(item.getProductImage());
                itemVo.setSkuSpecs(item.getSkuSpecs());
                itemVo.setPrice(item.getPrice());
                itemVo.setQuantity(item.getQuantity());
                itemVo.setTotalAmount(item.getTotalAmount());
                return itemVo;
            }).collect(Collectors.toList());

            OrderListItemVo vo = new OrderListItemVo();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setUserId(order.getUserId());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setPayAmount(order.getPayAmount());
            vo.setDiscountAmount(order.getDiscountAmount());
            vo.setFreightAmount(order.getFreightAmount());
            vo.setPayTime(order.getPayTime());
            vo.setExpireTime(order.getExpireTime());
            vo.setStatus(order.getStatus());
            vo.setRefundAmount(order.getRefundAmount());
            vo.setRejectReason(order.getRejectReason());
            vo.setRejectedAt(order.getRejectedAt());
            vo.setItems(itemVos);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 统计商家订单数量（支持多条件筛选）
     */
    @Override
    public int countSellerOrdersByFilters(Long sellerId,
                                          Integer status,
                                          Long userId,
                                          String orderNo,
                                          LocalDateTime payTimeStart,
                                          LocalDateTime payTimeEnd,
                                          LocalDateTime deliveryTimeStart,
                                          LocalDateTime deliveryTimeEnd,
                                          LocalDateTime receiveTimeStart,
                                          LocalDateTime receiveTimeEnd) {
        return orderMapper.countBySellerIdWithFilters(
                sellerId, status, userId, orderNo,
                payTimeStart, payTimeEnd, deliveryTimeStart, deliveryTimeEnd,
                receiveTimeStart, receiveTimeEnd);
    }

    /**
     * 商家获取订单详情（校验订单归属）
     */
    @Override
    public OrderDatailVo getOrderDetailBySellerId(String orderNo, Long sellerId) {
        log.info("商家获取订单详情, 商家ID: {}, 订单号: {}", sellerId, orderNo);
        Order order = orderMapper.selectByOrderNoAndSellerId(orderNo, sellerId);
        if (order == null) {
            log.warn("订单不属于该商家, 订单号: {}, 商家ID: {}", orderNo, sellerId);
            return null;
        }
        return getOrderById(order.getId());
    }

    /**
     * 管理员查询订单详情（不校验归属）
     */
    @Override
    public OrderDatailVo getOrderDetailByOrderNoForAdmin(String orderNo) {
        log.info("管理员查询订单详情, 订单号: {}", orderNo);
        Order order = orderMapper.selectByOrderNoForAdmin(orderNo);
        if (order == null) {
            log.warn("订单不存在, 订单号: {}", orderNo);
            return null;
        }
        return getOrderById(order.getId());
    }

    /**
     * 管理员分页查询全部订单（支持多条件筛选）
     * 不校验商家归属，可用于运营后台的订单管理。
     */
    @Override
    public List<Order> getAllOrders(Integer status,
                                    Long userId,
                                    String orderNo,
                                    LocalDateTime payTimeStart,
                                    LocalDateTime payTimeEnd,
                                    LocalDateTime deliveryTimeStart,
                                    LocalDateTime deliveryTimeEnd,
                                    LocalDateTime receiveTimeStart,
                                    LocalDateTime receiveTimeEnd,
                                    Integer page, Integer pageSize) {
        log.info("管理员分页查询全部订单（多条件）, status: {}, userId: {}, orderNo: {}, page: {}",
                status, userId, orderNo, page);
        Page<Order> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        IPage<Order> result = orderMapper.selectAllOrdersWithFilters(
                pageParam, status, userId, orderNo,
                payTimeStart, payTimeEnd, deliveryTimeStart, deliveryTimeEnd,
                receiveTimeStart, receiveTimeEnd);
        return result.getRecords();
    }

    /**
     * 统计全部订单数量（支持多条件筛选，管理员用）
     */
    @Override
    public int countAllOrders(Integer status,
                              Long userId,
                              String orderNo,
                              LocalDateTime payTimeStart,
                              LocalDateTime payTimeEnd,
                              LocalDateTime deliveryTimeStart,
                              LocalDateTime deliveryTimeEnd,
                              LocalDateTime receiveTimeStart,
                              LocalDateTime receiveTimeEnd) {
        return orderMapper.countAllOrdersWithFilters(
                status, userId, orderNo,
                payTimeStart, payTimeEnd, deliveryTimeStart, deliveryTimeEnd,
                receiveTimeStart, receiveTimeEnd);
    }

    /**
     * 管理员强制取消订单
     * <p>
     * 不校验订单状态，根据当前状态执行不同的库存回滚策略：
     * 待付款 → 释放冻结库存；待发货 → 取消已确认的库存。
     * 事务提交后异步同步库存到 MySQL。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminCancelOrder(Long orderId, String cancelReason) {
        log.info("管理员强制取消订单, 订单ID: {}, 原因: {}", orderId, cancelReason);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在, ID: {}", orderId);
            return false;
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        List<Long> skuIds = new ArrayList<>();
        for (OrderItem item : items) {
            if (order.getStatus() == 1) {
                stockLuaScript.releaseStock(item.getSkuId(), item.getQuantity());
            } else if (order.getStatus() == 2) {
                stockLuaScript.cancelStock(item.getSkuId(), item.getQuantity());
            }
            skuIds.add(item.getSkuId());
        }

        order.setStatus(5);
        order.setCancelReason("运营操作: " + (cancelReason != null ? cancelReason : "管理员强制取消"));
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("管理员强制取消订单成功, 订单ID: {}", orderId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderEventPublisher.publishStockSync(skuIds);
            }
        });
        return true;
    }

    /**
     * 管理员调整订单金额
     * 仅待付款状态的订单可调整，支持修改运费、优惠金额和实付金额。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustOrderAmount(Long orderId, BigDecimal freightAmount, BigDecimal discountAmount, BigDecimal payAmount) {
        log.info("管理员调整订单金额, 订单ID: {}, 运费: {}, 优惠: {}, 实付: {}", orderId, freightAmount, discountAmount, payAmount);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在, ID: {}", orderId);
            return false;
        }

        if (order.getStatus() != 1) {
            log.warn("订单状态不允许调整金额, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        if (payAmount == null) {
            log.warn("实付金额不能为空, 订单ID: {}", orderId);
            return false;
        }

        if (freightAmount != null) {
            order.setFreightAmount(freightAmount);
        }
        if (discountAmount != null) {
            order.setDiscountAmount(discountAmount);
        }
        order.setPayAmount(payAmount);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("管理员调整订单金额成功, 订单ID: {}", orderId);
        return true;
    }

    /**
     * 用户申请退款
     * 仅已支付的订单可申请退款，记录退款原因和原状态，订单进入退款中状态。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyRefund(Long orderId, String refundReason) {
        log.info("用户申请退款, 订单ID: {}, 原因: {}", orderId, refundReason);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            log.warn("订单不存在或已删除, ID: {}", orderId);
            return false;
        }

        if (order.getPayStatus() != 1) {
            log.warn("订单未支付，无法申请退款, 订单ID: {}, 支付状态: {}", orderId, order.getPayStatus());
            return false;
        }

        order.setRefundFromStatus(order.getStatus());
        order.setStatus(6);
        order.setRefundReason(refundReason);
        order.setRefundAmount(order.getPayAmount());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("用户申请退款成功, 订单ID: {}", orderId);
        return true;
    }

    /**
     * 用户取消退款申请
     * 仅退款中或已拒绝状态的订单可取消退款，恢复为退款前的订单状态。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRefund(Long orderId, Long userId) {
        log.info("用户取消退款申请, 订单ID: {}, 用户ID: {}", orderId, userId);

        Order order = orderMapper.selectById(orderId);

        if (order == null || order.getIsDeleted() == 1) {
            log.warn("订单不存在或已删除, ID: {}", orderId);
            return false;
        }

        if (!order.getUserId().equals(userId)) {
            log.warn("无权取消此订单的退款, 订单用户ID: {}, 当前用户ID: {}", order.getUserId(), userId);
            return false;
        }

        if (order.getStatus() != 6 && order.getStatus() != 8) {
            log.warn("当前订单状态不允许取消退款, 状态: {}", order.getStatus());
            return false;
        }

        Integer fromStatus = order.getRefundFromStatus();
        if (fromStatus == null) {
            fromStatus = 4;
        }

        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<Order>()
                .set(Order::getStatus, fromStatus)
                .set(Order::getRefundFromStatus, null)
                .set(Order::getRefundReason, null)
                .set(Order::getRefundAmount, null)
                .set(Order::getRejectReason, null)
                .set(Order::getRejectedAt, null)
                .set(Order::getUpdatedAt, LocalDateTime.now())
                .eq(Order::getId, orderId);
        orderMapper.update(order, updateWrapper);

        log.info("用户取消退款成功, 订单ID: {}, 恢复状态: {}", orderId, fromStatus);
        return true;
    }

    /**
     * 管理员审核通过退款
     * 释放 Redis 中已确认的库存，更新订单状态为"已退款"，支付状态为"已退款"。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveRefund(Long orderId, Long operatorId) {
        log.info("管理员审核通过退款, 订单ID: {}, 操作人ID: {}", orderId, operatorId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在, ID: {}", orderId);
            return false;
        }

        if (order.getStatus() != 6) {
            log.warn("订单不在退款中状态, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        List<Long> skuIds = new ArrayList<>();
        for (OrderItem item : items) {
            stockLuaScript.releaseStock(item.getSkuId(), item.getQuantity());
            skuIds.add(item.getSkuId());
        }

        order.setStatus(7);
        order.setPayStatus(2);
        order.setRefundOperatorId(operatorId);
        order.setRefundAmount(order.getPayAmount());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("退款审核通过完成, 订单ID: {}", orderId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderEventPublisher.publishStockSync(skuIds);
            }
        });
        return true;
    }

    /**
     * 管理员拒绝退款
     * 更新订单状态为"已拒绝"，记录拒绝原因和操作人。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectRefund(Long orderId, String rejectReason, Long operatorId) {
        log.info("管理员拒接退款, 订单ID: {}, 原因: {}, 操作人ID: {}", orderId, rejectReason, operatorId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在, ID: {}", orderId);
            return false;
        }

        if (order.getStatus() != 6) {
            log.warn("订单不在退款中状态, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        if (rejectReason == null || rejectReason.isEmpty()) {
            log.warn("拒接退款时拒绝原因不能为空, 订单ID: {}", orderId);
            return false;
        }

        order.setStatus(8);
        order.setRejectReason(rejectReason);
        order.setRejectedAt(LocalDateTime.now());
        order.setRefundOperatorId(operatorId);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("拒接退款完成, 订单ID: {}, 原因: {}", orderId, rejectReason);
        return true;
    }

    /**
     * 查询退款进度
     * 根据订单当前状态返回退款进度码和描述（退款中/已退款/已拒绝/未申请）。
     */
    @Override
    public RefundProgressVo getRefundProgress(Long orderId) {
        log.info("查询退款进度, 订单ID: {}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在, ID: {}", orderId);
            return null;
        }

        RefundProgressVo vo = new RefundProgressVo();
        vo.setRefundReason(order.getRefundReason());
        vo.setRefundAmount(order.getRefundAmount());

        Integer status = order.getStatus() != null ? order.getStatus() : 0;
        switch (status) {
            case 6:
                vo.setRefundStatusCode(1);
                vo.setRefundStatusDesc("退款中");
                vo.setApplyTime(order.getUpdatedAt());
                break;
            case 7:
                vo.setRefundStatusCode(2);
                vo.setRefundStatusDesc("已退款");
                vo.setApplyTime(order.getUpdatedAt());
                vo.setReviewTime(order.getUpdatedAt());
                break;
            case 8:
                vo.setRefundStatusCode(3);
                vo.setRefundStatusDesc("已拒绝");
                vo.setRejectReason(order.getRejectReason());
                vo.setReviewTime(order.getRejectedAt());
                break;
            default:
                vo.setRefundStatusCode(0);
                vo.setRefundStatusDesc("未申请退款");
                break;
        }

        log.info("退款进度查询结果, 订单ID: {}, 状态: {}", orderId, vo.getRefundStatusDesc());
        return vo;
    }

    /**
     * 获取订单状态的中文描述
     */
    @Override
    public String getStatusDesc(Integer status) {
        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(1, "待付款");
        statusMap.put(2, "待发货");
        statusMap.put(3, "待收货");
        statusMap.put(4, "已完成");
        statusMap.put(5, "已取消");
        statusMap.put(6, "退款中");
        statusMap.put(7, "已退款");
        statusMap.put(8, "已拒绝");
        return statusMap.getOrDefault(status, "未知状态");
    }

    /**
     * 获取支付状态的中文描述
     */
    @Override
    public String getPayStatusDesc(Integer payStatus) {
        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(0, "未支付");
        statusMap.put(1, "已支付");
        statusMap.put(2, "已退款");
        return statusMap.getOrDefault(payStatus, "未知状态");
    }

    /**
     * 获取支付方式的中文描述
     */
    @Override
    public String getPayTypeDesc(String payType) {
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("alipay", "支付宝");
        typeMap.put("wechat", "微信支付");
        return typeMap.getOrDefault(payType, "未支付");
    }

    private String generateOrderNo() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis());
        sb.append(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return sb.toString();
    }
}