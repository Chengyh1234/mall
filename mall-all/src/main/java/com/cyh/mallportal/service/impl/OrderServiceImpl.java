package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyh.mallcommon.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallportal.dto.OrderCreateDto;
import com.cyh.mallportal.dto.OrderDeliveryDto;
import com.cyh.mallportal.dto.OrderItemDto;
import com.cyh.mallportal.entity.Address;
import com.cyh.mallportal.entity.CartItem;
import com.cyh.mallportal.entity.Order;
import com.cyh.mallportal.entity.OrderDelivery;
import com.cyh.mallportal.entity.OrderItem;
import com.cyh.mallportal.entity.Sku;
import com.cyh.mallportal.mapper.AddressMapper;
import com.cyh.mallportal.mapper.CartItemMapper;
import com.cyh.mallportal.mapper.OrderDeliveryMapper;
import com.cyh.mallportal.mapper.OrderItemMapper;
import com.cyh.mallportal.mapper.OrderMapper;
import com.cyh.mallportal.mapper.SkuMapper;
import com.cyh.mallportal.mapper.SpuMapper;
import com.cyh.mallportal.service.CartItemService;
import com.cyh.mallportal.service.InventoryRedisService;
import com.cyh.mallportal.service.OrderDeliveryService;
import com.cyh.mallportal.service.OrderService;
import com.cyh.mallportal.service.StockLuaScript;
import com.cyh.mallportal.vo.OrderListItemVo;
import com.cyh.mallportal.vo.OrderStatusCountVo;
import com.cyh.mallportal.vo.OrderVo;
import com.cyh.mallportal.vo.RefundProgressVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AddressMapper addressMapper;
    private final SkuMapper skuMapper;
    private final OrderDeliveryMapper orderDeliveryMapper;
    private final OrderDeliveryService orderDeliveryService;
    private final CartItemMapper cartItemMapper;
    private final CartItemService cartItemService;
    private final StockLuaScript stockLuaScript;
    private final InventoryRedisService inventoryRedisService;
    private final SpuMapper spuMapper;

    /**
     * 支付超时时间（分钟），默认 30 分钟
     */
    @Value("${mall.order.pay-expire-minutes:30}")
    private int payExpireMinutes;

    /**
     * 创建订单
     * 使用 Redis Lua 脚本冻结库存，设置支付超时时间
     *
     * @param userId         用户ID
     * @param orderCreateDto 订单创建DTO
     * @return 订单VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long userId, OrderCreateDto orderCreateDto) {
        log.info("创建订单, 用户ID: {}", userId);

        // 1. 生成订单号
        String orderNo = generateOrderNo();

        // 2. 获取收货地址信息
        Address address = null;
        if (orderCreateDto.getAddressId() != null) {
            address = addressMapper.selectById(orderCreateDto.getAddressId());
        }

        // 3. 冻结库存（原子操作，任一 SKU 失败则回滚已冻结的）
        List<Long> frozenSkuIds = new ArrayList<>();
        try {
            for (OrderItemDto itemDto : orderCreateDto.getItems()) {
                Sku sku = skuMapper.selectById(itemDto.getSkuId());
                if (sku == null) {
                    rollbackFrozen(frozenSkuIds, orderCreateDto.getItems());
                    throw new BusinessException("SKU不存在, SKU ID: " + itemDto.getSkuId());
                }
                boolean success = stockLuaScript.freezeStock(itemDto.getSkuId(), itemDto.getQuantity());
                if (!success) {
                    rollbackFrozen(frozenSkuIds, orderCreateDto.getItems());
                    throw new BusinessException("库存不足, SKU: " + sku.getId());
                }
                frozenSkuIds.add(itemDto.getSkuId());
            }
        } catch (BusinessException e) {
            throw e;
        }

        // 4. 创建订单实体
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

        // 5. 创建订单明细
        for (OrderItemDto itemDto : orderCreateDto.getItems()) {
            Sku sku = skuMapper.selectById(itemDto.getSkuId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderNo(orderNo);
            orderItem.setSkuId(itemDto.getSkuId());
            orderItem.setSpuId(sku.getSpuId());
            orderItem.setProductName(itemDto.getProductName() != null ? itemDto.getProductName() : "");
            orderItem.setProductImage(itemDto.getProductImage() != null ? itemDto.getProductImage() : sku.getImage());
            orderItem.setSkuSpecs(itemDto.getSkuSpecs());
            orderItem.setPrice(itemDto.getPrice() != null ? itemDto.getPrice() : sku.getPrice());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setTotalAmount(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
            orderItem.setGiftFlag(itemDto.getGiftFlag() != null ? itemDto.getGiftFlag() : 0);
            orderItem.setCreatedAt(LocalDateTime.now());

            orderItemMapper.insert(orderItem);

            // 同步 Redis 库存到 MySQL
            inventoryRedisService.syncStockToDb(itemDto.getSkuId());
        }

        log.info("订单明细创建完成, 订单ID: {}", order.getId());
        return order.getId();
    }

    /**
     * 从购物车创建订单（结算）
     * 使用 Redis Lua 脚本冻结库存，设置支付超时时间
     *
     * @param userId       用户ID
     * @param addressId    收货地址ID
     * @param buyerMessage 买家留言（可选）
     * @return 订单VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createOrderFromCart(Long userId, Long addressId, String buyerMessage) {
        log.info("从购物车创建订单, 用户ID: {}, 地址ID: {}", userId, addressId);

        // 1. 获取已选中的购物车商品
        List<CartItem> selectedItems = cartItemService.getSelectedItems(userId);
        if (selectedItems == null || selectedItems.isEmpty()) {
            log.warn("购物车中没有选中的商品");
            return Collections.emptyList();
        }

        // 2. 获取收货地址信息
        Address address = addressMapper.selectById(addressId);
        if (address == null) {
            log.warn("收货地址不存在, {}", addressId);
            return Collections.emptyList();
        }

        // 3. 冻结库存（原子操作，任一 SKU 失败则回滚已冻结的）
        List<Long> frozenSkuIds = new ArrayList<>();

        for (CartItem item : selectedItems) {
            boolean success = stockLuaScript.freezeStock(item.getSkuId(), item.getQuantity());
            if (!success) {
                rollbackFrozenByCart(frozenSkuIds, selectedItems);
                throw new BusinessException("商品[" + item.getProductName() + "]库存不足");
            }
            frozenSkuIds.add(item.getSkuId());
        }

        // 4. 遍历购物车商品，每个 SKU 生成 1 笔独立订单
        List<Long> resultList = new ArrayList<>();
        for (CartItem item : selectedItems) {
            // 生成订单号
            String orderNo = generateOrderNo();

            // 创建订单（每笔订单只含 1 种商品）
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

            // 创建 1 条订单明细
            Sku sku = skuMapper.selectById(item.getSkuId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderNo(orderNo);
            orderItem.setSkuId(item.getSkuId());
            orderItem.setSpuId(sku.getSpuId());
            orderItem.setProductName(item.getProductName() != null ? item.getProductName() : "");
            orderItem.setProductImage(item.getProductImage() != null ? item.getProductImage() : sku.getImage());
            orderItem.setSkuSpecs(item.getSkuSpecs());
            orderItem.setPrice(item.getPrice() != null ? item.getPrice() : sku.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setTotalAmount(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
            orderItem.setGiftFlag(0);
            orderItem.setCreatedAt(LocalDateTime.now());

            orderItemMapper.insert(orderItem);

            // 同步 Redis 库存到 MySQL
            inventoryRedisService.syncStockToDb(item.getSkuId());

            // 只记录订单ID而非完整VO，前端如需详情可调用订单详情接口
            resultList.add(order.getId());
        }

        // 5. 清空已选中的购物车商品
        cartItemService.clearSelected(userId);
        log.info("已选中购物车商品已清空, 用户ID: {}", userId);

        log.info("从购物车创建订单完成, 用户ID: {}, 共 {} 笔订单", userId, resultList.size());
        return resultList;
    }

    /**
     * 回滚已冻结的库存（下单失败时）
     */
    private void rollbackFrozen(List<Long> frozenSkuIds, List<OrderItemDto> items) {
        for (OrderItemDto itemDto : items) {
            if (frozenSkuIds.contains(itemDto.getSkuId())) {
                stockLuaScript.releaseStock(itemDto.getSkuId(), itemDto.getQuantity());
            }
        }
    }

    /**
     * 回滚已冻结的库存（购物车下单失败时）
     */
    private void rollbackFrozenByCart(List<Long> frozenSkuIds, List<CartItem> items) {
        for (CartItem item : items) {
            if (frozenSkuIds.contains(item.getSkuId())) {
                stockLuaScript.releaseStock(item.getSkuId(), item.getQuantity());
            }
        }
    }

    /**
     * 根据ID获取订单详情
     *
     * @param id 订单ID
     * @return 订单VO
     */
    @Override
    public OrderVo getOrderById(Long id) {
        log.info("获取订单详情, 订单ID: {}", id);

        Order order = orderMapper.selectById(id);
        if (order == null) {
            return null;
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(id);
        List<OrderDelivery> deliveries = orderDeliveryMapper.selectByOrderId(id);

        // 组装订单项VO
        List<OrderVo.ItemVo> itemVos = items.stream().map(item -> {
            OrderVo.ItemVo itemVo = new OrderVo.ItemVo();
            itemVo.setProductName(item.getProductName());
            itemVo.setProductImage(item.getProductImage());
            itemVo.setSkuSpecs(item.getSkuSpecs());
            itemVo.setPrice(item.getPrice());
            itemVo.setQuantity(item.getQuantity());
            itemVo.setTotalAmount(item.getTotalAmount());
            return itemVo;
        }).collect(Collectors.toList());

        // 组装发货记录VO
        List<OrderVo.DeliveryVo> deliveryVos = deliveries.stream().map(d -> {
            OrderVo.DeliveryVo deliveryVo = new OrderVo.DeliveryVo();
            deliveryVo.setDeliveryCompany(d.getDeliveryCompany());
            deliveryVo.setDeliveryNo(d.getDeliveryNo());
            deliveryVo.setDeliveryStatus(d.getDeliveryStatus());
            deliveryVo.setDeliveryTime(d.getDeliveryTime());
            return deliveryVo;
        }).collect(Collectors.toList());

        OrderVo vo = new OrderVo();
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
     * 根据ID获取订单详情（用户端使用，过滤 is_deleted=1 的已删除订单）
     *
     * @param id 订单ID
     * @return 订单VO，已删除时返回 null
     */
    @Override
    public OrderVo getOrderByIdForUser(Long id) {
        log.info("获取订单详情（用户过滤）, 订单ID: {}", id);

        Order order = orderMapper.selectById(id);
        // 已删除的订单对用户不可见
        if (order == null || order.getIsDeleted() == 1) {
            log.warn("订单不存在或已删除, 订单ID: {}", id);
            return null;
        }

        return getOrderById(id);
    }

    /**
     * 根据订单号获取订单详情
     *
     * @param orderNo 订单号
     * @return 订单VO
     */
    @Override
    public OrderVo getOrderByOrderNo(String orderNo) {
        log.info("获取订单详情, 订单号: {}", orderNo);

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return null;
        }

        return getOrderById(order.getId());
    }

    /**
     * 分页获取用户订单列表（带商品明细，自动过滤 is_deleted=0）
     * 先分页查订单，再批量加载商品明细，避免 N+1 查询
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @return 订单列表项VO列表
     */
    @Override
    public List<OrderListItemVo> getOrderListWithItems(Long userId, Integer status, Integer page, Integer pageSize) {
        log.info("分页获取用户订单列表（带商品明细）, 用户ID: {}, 状态: {}, 页码: {}, 每页: {}", userId, status, page, pageSize);

        // 1. 分页查询用户订单（按状态筛选，status 为 null 时查全部）
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

        // 2. 收集订单ID，批量加载商品明细
        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        List<OrderItem> allItems = orderItemMapper.selectByOrderIds(orderIds);

        // 3. 按订单ID分组
        Map<Long, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        // 4. 组装 VO
        return orders.stream().map(order -> {
            List<OrderItem> items = itemMap.getOrDefault(order.getId(), Collections.emptyList());

            // 转换商品明细
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

            // 转换订单信息
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
     * 取消订单
     * 释放 Redis 中冻结的库存
     *
     * @param orderId      订单ID
     * @param cancelReason 取消原因
     * @return 是否取消成功
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

        // 只有待付款状态才能取消
        if (order.getStatus() != 1) {
            log.warn("订单状态不允许取消, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        // 释放 Redis 冻结库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            stockLuaScript.releaseStock(item.getSkuId(), item.getQuantity());
            // 同步释放后的库存到 MySQL
            inventoryRedisService.syncStockToDb(item.getSkuId());
        }

        order.setStatus(5);
        order.setCancelReason(cancelReason);
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.updateById(order);
        log.info("订单取消成功, 订单ID: {}", orderId);
        return true;
    }

    /**
     * 支付订单
     * 确认扣除 Redis 冻结库存（冻结 → 实扣）
     *
     * @param orderId 订单ID
     * @param payType 支付方式
     * @return 是否支付成功
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

        // 只有待付款状态才能支付
        if (order.getStatus() != 1) {
            log.warn("订单状态不允许支付, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        // 检查是否已超时
        if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
            log.warn("订单已超时, 订单ID: {}", orderId);
            return false;
        }

        // 确认扣除库存（冻结 → 实扣）
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            stockLuaScript.confirmStock(item.getSkuId(), item.getQuantity());
            // 同步库存到 MySQL
            inventoryRedisService.syncStockToDb(item.getSkuId());
        }

        order.setPayStatus(1);
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        order.setStatus(2);
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.updateById(order);
        log.info("订单支付成功, 订单ID: {}", orderId);
        return true;
    }

    /**
     * 批量付款
     * 对当前用户的多笔待付款订单进行批量支付，已支付或失败的订单不会影响其他订单
     *
     * @param orderIds 订单ID列表
     * @param payType  支付方式（alipay/wechat）
     * @param userId   当前用户ID
     * @return 批量付款结果汇总
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchPayOrders(List<Long> orderIds, String payType, Long userId) {
        log.info("批量付款, 用户ID: {}, 订单数: {}, 支付方式: {}", userId, orderIds.size(), payType);

        // 校验结果容器
        List<Long> successIds = new ArrayList<>();
        List<Map<String, Object>> failList = new ArrayList<>();

        // 批量查询订单（自动过滤 is_deleted=0 且属于该用户）
        List<Order> orders = orderMapper.selectByIdsAndUserId(orderIds, userId);
        if (orders.isEmpty()) {
            throw new BusinessException("未找到可支付的订单");
        }

        // 按 ID 建立查找映射，方便判断哪些 ID 未被找到
        Map<Long, Order> orderMap = orders.stream()
                .collect(Collectors.toMap(Order::getId, o -> o));

        // 检查是否有不存在的订单 ID
        for (Long id : orderIds) {
            if (!orderMap.containsKey(id)) {
                Map<String, Object> failItem = new HashMap<>();
                failItem.put("orderId", id);
                failItem.put("reason", "订单不存在或不属于当前用户");
                failList.add(failItem);
            }
        }

        // 遍历查询到的订单，逐一校验并支付
        for (Order order : orders) {
            Long orderId = order.getId();
            Map<String, Object> failItem = new HashMap<>();
            failItem.put("orderId", orderId);

            // 状态校验：仅待付款可支付
            if (order.getStatus() != 1) {
                failItem.put("reason", "订单状态不允许支付（当前状态: " + order.getStatus() + "）");
                failList.add(failItem);
                continue;
            }

            // 超时校验
            if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
                failItem.put("reason", "订单已超时，无法支付");
                failList.add(failItem);
                continue;
            }

            // 扣减库存（冻结 → 实扣）
            List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
            for (OrderItem item : items) {
                stockLuaScript.confirmStock(item.getSkuId(), item.getQuantity());
                inventoryRedisService.syncStockToDb(item.getSkuId());
            }

            // 更新订单状态
            order.setPayStatus(1);
            order.setPayType(payType);
            order.setPayTime(LocalDateTime.now());
            order.setStatus(2);
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(order);

            successIds.add(orderId);
            log.info("批量付款 - 订单支付成功, 订单ID: {}", orderId);
        }

        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("success", successIds);
        result.put("fail", failList);
        result.put("totalCount", orderIds.size());
        result.put("successCount", successIds.size());
        result.put("failCount", failList.size());

        log.info("批量付款完成, 成功: {}, 失败: {}", successIds.size(), failList.size());
        return result;
    }

    /**
     * 发货
     *
     * @param orderId         订单ID
     * @param deliveryCompany 物流公司
     * @param deliveryNo      物流单号
     * @return 是否发货成功
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

        // 只有待发货状态才能发货
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

        // 创建发货记录
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
     *
     * @param orderId 订单ID
     * @return 是否确认成功
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

        // 只有待收货状态才能确认收货
        if (order.getStatus() != 3) {
            log.warn("订单状态不允许确认收货, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        order.setStatus(4);
        order.setReceiveTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.updateById(order);

        // 更新 SPU 销量：确认收货后累加销量, 不用清除 SPU 缓存，销量给个大概就行。
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                spuMapper.increaseSales(item.getSpuId(), item.getQuantity());
            }
        }

        log.info("订单确认收货成功, 订单ID: {}", orderId);
        return true;
    }

    /**
     * 删除订单（逻辑删除）
     * 仅允许删除"已取消(5)"或"已完成(4)"的订单
     *
     * @param orderId 订单ID
     * @return 是否删除成功
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

        // 仅允许"已取消(5)"或"已完成(4)"的订单进行删除
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
     * 获取用户订单数量
     *
     * @param userId 用户ID
     * @return 订单数量
     */
    @Override
    public int countOrders(Long userId) {
        return orderMapper.countByUserId(userId);
    }

    /**
     * 统计用户指定状态订单数量（自动过滤 is_deleted=0）
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单数量
     */
    @Override
    public int countOrders(Long userId, Integer status) {
        return orderMapper.countByUserIdAndStatus(userId, status);
    }

    /**
     * 批量统计用户各状态订单数量
     * 一次性查询待付款/待发货/待收货/退款中的数量，用于前端"我的订单"各标签角标
     *
     * @param userId 用户ID
     * @return 各状态订单数量 VO
     */
    @Override
    public OrderStatusCountVo countOrderStatusByUserId(Long userId) {
        log.info("批量统计用户各状态订单数量, 用户ID: {}", userId);
        return orderMapper.countOrderStatusByUserId(userId);
    }

    // ==================== 商家订单查询 ====================

    /**
     * 分页查询商家店铺订单列表
     *
     * @param sellerId 商家用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    @Override
    public List<Order> getOrdersBySellerId(Long sellerId, Integer page, Integer pageSize) {
        log.info("分页获取商家订单列表, 商家ID: {}, 页码: {}, 每页: {}", sellerId, page, pageSize);
        Page<Order> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        IPage<Order> result = orderMapper.selectBySellerId(pageParam, sellerId);
        return result.getRecords();
    }

    /**
     * 分页查询商家店铺订单列表（按状态筛选）
     *
     * @param sellerId 商家用户ID
     * @param status   订单状态
     * @param page     页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    @Override
    public List<Order> getOrdersBySellerIdAndStatus(Long sellerId, Integer status, Integer page, Integer pageSize) {
        log.info("分页获取商家订单列表, 商家ID: {}, 状态: {}, 页码: {}, 每页: {}", sellerId, status, page, pageSize);
        Page<Order> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        IPage<Order> result = orderMapper.selectBySellerIdAndStatus(pageParam, sellerId, status);
        return result.getRecords();
    }

    /**
     * 统计商家店铺订单总数
     */
    @Override
    public int countOrdersBySellerId(Long sellerId) {
        return orderMapper.countBySellerId(sellerId);
    }

    /**
     * 统计商家店铺指定状态订单总数
     */
    @Override
    public int countOrdersBySellerIdAndStatus(Long sellerId, Integer status) {
        return orderMapper.countBySellerIdAndStatus(sellerId, status);
    }

    /**
     * 商家分页查询店铺订单列表（带商品明细，多条件筛选）
     * 返回 OrderListItemVo，支持按状态、用户ID、订单号、支付/发货/收货时间范围筛选
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

        // 1. 分页查询（使用 MyBatis-Plus 分页插件，自动处理 LIMIT/OFFSET）
        Page<Order> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        IPage<Order> orderPage = orderMapper.selectBySellerIdWithFilters(
                pageParam, sellerId, status, userId, orderNo,
                payTimeStart, payTimeEnd, deliveryTimeStart, deliveryTimeEnd,
                receiveTimeStart, receiveTimeEnd);
        List<Order> orders = orderPage.getRecords();

        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 收集订单ID，批量加载商品明细
        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        List<OrderItem> allItems = orderItemMapper.selectByOrderIds(orderIds);

        // 3. 按订单ID分组
        Map<Long, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        // 4. 组装 VO
        return orders.stream().map(order -> {
            List<OrderItem> items = itemMap.getOrDefault(order.getId(), Collections.emptyList());

            // 转换商品明细
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

            // 转换订单信息
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
     * 统计商家店铺订单总数（多条件筛选）
     * 与 getSellerOrderListWithItems 条件完全一致，用于分页总记录数
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
     * 商家获取订单详情
     * 先校验订单归属，再返回完整详情
     *
     * @param orderNo  订单号
     * @param sellerId 商家用户ID
     * @return 订单详情 VO，不属于该商家时返回 null
     */
    @Override
    public OrderVo getOrderDetailBySellerId(String orderNo, Long sellerId) {
        log.info("商家获取订单详情, 商家ID: {}, 订单号: {}", sellerId, orderNo);
        Order order = orderMapper.selectByOrderNoAndSellerId(orderNo, sellerId);
        if (order == null) {
            log.warn("订单不属于该商家, 订单号: {}, 商家ID: {}", orderNo, sellerId);
            return null;
        }
        return getOrderById(order.getId());
    }

    // ==================== 管理员订单查询（不过滤 is_deleted） ====================

    /**
     * 管理员根据订单号查询订单详情（不过滤 is_deleted，可查看所有订单包括已删除的）
     *
     * @param orderNo 订单号
     * @return 订单详情 VO
     */
    @Override
    public OrderVo getOrderDetailByOrderNoForAdmin(String orderNo) {
        log.info("管理员查询订单详情, 订单号: {}", orderNo);
        Order order = orderMapper.selectByOrderNoForAdmin(orderNo);
        if (order == null) {
            log.warn("订单不存在, 订单号: {}", orderNo);
            return null;
        }
        return getOrderById(order.getId());
    }

    // ==================== 全局订单查询（运营管理员/超级管理员） ====================

    /**
     * 分页查询全部订单（运营管理员/超级管理员使用，多条件筛选）
     * 查询平台所有订单，支持按状态、用户ID、订单号、支付/发货/收货时间范围筛选
     *
     * @param status            订单状态（可选，null 时查询全部）
     * @param userId            用户ID（可选）
     * @param orderNo           订单号（可选，模糊匹配）
     * @param payTimeStart      支付时间范围-起始（可选）
     * @param payTimeEnd        支付时间范围-结束（可选）
     * @param deliveryTimeStart 发货时间范围-起始（可选）
     * @param deliveryTimeEnd   发货时间范围-结束（可选）
     * @param receiveTimeStart  收货时间范围-起始（可选）
     * @param receiveTimeEnd    收货时间范围-结束（可选）
     * @param page              页码，默认第1页
     * @param pageSize          每页数量，默认10条
     * @return 订单列表
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
     * 统计全部订单总数（多条件筛选）
     * 与 getAllOrders 条件完全一致，用于分页总记录数
     *
     * @param status            订单状态（可选，null 时统计全部）
     * @param userId            用户ID（可选）
     * @param orderNo           订单号（可选，模糊匹配）
     * @param payTimeStart      支付时间范围-起始（可选）
     * @param payTimeEnd        支付时间范围-结束（可选）
     * @param deliveryTimeStart 发货时间范围-起始（可选）
     * @param deliveryTimeEnd   发货时间范围-结束（可选）
     * @param receiveTimeStart  收货时间范围-起始（可选）
     * @param receiveTimeEnd    收货时间范围-结束（可选）
     * @return 订单总数
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

    // ==================== 运营管理员操作 ====================

    /**
     * 管理员强制取消订单
     * 可取消任意状态的订单，释放库存并记录运营取消原因
     *
     * @param orderId      订单ID
     * @param cancelReason 取消原因（运营操作）
     * @return 是否取消成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminCancelOrder(Long orderId, String cancelReason) {
        log.info("管理员强制取消订单, 订单ID: {}, 原因: {}", orderId, cancelReason);

        // 1. 校验订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在, ID: {}", orderId);
            return false;
        }

        // 2. 释放库存（无论原状态如何，只要有已支付/冻结库存就释放）
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            stockLuaScript.releaseStock(item.getSkuId(), item.getQuantity());
            // 同步释放后的库存到 MySQL
            inventoryRedisService.syncStockToDb(item.getSkuId());
        }

        // 3. 更新订单状态为"已取消"，记录运营原因
        order.setStatus(5);
        order.setCancelReason("运营操作: " + (cancelReason != null ? cancelReason : "管理员强制取消"));
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("管理员强制取消订单成功, 订单ID: {}", orderId);
        return true;
    }

    /**
     * 管理员调整订单金额
     * 仅限待付款订单（status=1），调整运费、优惠金额和实付金额
     *
     * @param orderId        订单ID
     * @param freightAmount  调整后的运费（可选）
     * @param discountAmount 调整后的优惠金额（可选）
     * @param payAmount      调整后的实付金额（必填）
     * @return 是否调整成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustOrderAmount(Long orderId, BigDecimal freightAmount, BigDecimal discountAmount, BigDecimal payAmount) {
        log.info("管理员调整订单金额, 订单ID: {}, 运费: {}, 优惠: {}, 实付: {}", orderId, freightAmount, discountAmount, payAmount);

        // 1. 校验订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在, ID: {}", orderId);
            return false;
        }

        // 2. 仅限待付款订单（status=1）
        if (order.getStatus() != 1) {
            log.warn("订单状态不允许调整金额, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        // 3. 实付金额必填校验
        if (payAmount == null) {
            log.warn("实付金额不能为空, 订单ID: {}", orderId);
            return false;
        }

        // 4. 更新金额字段
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
     * 仅限已支付订单（payStatus=1），将订单状态置为"退款中"
     *
     * @param orderId      订单ID
     * @param refundReason 退款原因
     * @return 是否申请成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyRefund(Long orderId, String refundReason) {
        log.info("用户申请退款, 订单ID: {}, 原因: {}", orderId, refundReason);

        // 1. 校验订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            log.warn("订单不存在或已删除, ID: {}", orderId);
            return false;
        }

        // 2. 仅限已支付订单（payStatus=1）
        if (order.getPayStatus() != 1) {
            log.warn("订单未支付，无法申请退款, 订单ID: {}, 支付状态: {}", orderId, order.getPayStatus());
            return false;
        }

        // 3. 记录退款来源状态（取消退款时恢复），然后更新订单状态为"退款中"（status=6）
        order.setRefundFromStatus(order.getStatus());  // 记录退款前状态: 2-待发货 3-待收货 4-已完成
        order.setStatus(6);
        order.setRefundReason(refundReason);
        order.setRefundAmount(order.getPayAmount());  // 退款金额默认实付金额
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("用户申请退款成功, 订单ID: {}", orderId);
        return true;
    }

    /**
     * 用户取消退款申请
     * 仅允许 status=6（退款中）或 8（已拒绝）的订单取消，
     * 取消后恢复到退款前的订单状态（refund_from_status）
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 是否取消成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRefund(Long orderId, Long userId) {
        log.info("用户取消退款申请, 订单ID: {}, 用户ID: {}", orderId, userId);

        // 1. 校验订单是否存在、是否已删除
        Order order = orderMapper.selectById(orderId);

        if (order == null || order.getIsDeleted() == 1) {
            log.warn("订单不存在或已删除, ID: {}", orderId);
            return false;
        }

        // 2. 校验订单归属
        if (!order.getUserId().equals(userId)) {
            log.warn("无权取消此订单的退款, 订单用户ID: {}, 当前用户ID: {}", order.getUserId(), userId);
            return false;
        }

        // 3. 校验订单状态为 6（退款中）或 8（已拒绝）才允许取消
        if (order.getStatus() != 6 && order.getStatus() != 8) {
            log.warn("当前订单状态不允许取消退款, 状态: {}", order.getStatus());
            return false;
        }

        // 4. 恢复退款前状态（refund_from_status 在申请退款时记录）
        Integer fromStatus = order.getRefundFromStatus();
        if (fromStatus == null) {
            // 兼容旧数据没有 refund_from_status，默认回到"已完成"
            fromStatus = 4;
        }
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<Order>()
                .set(Order::getStatus, fromStatus)
                .set(Order::getRefundFromStatus, null)
                .set(Order::getRefundReason, null)
                .set(Order::getRefundAmount, null)
                .set(Order::getRejectReason, null) // 清除拒绝原因
                .set(Order::getRejectedAt, null)   // 清除拒绝时间
                .set(Order::getUpdatedAt, LocalDateTime.now())
                .eq(Order::getId, orderId);
        orderMapper.update(order, updateWrapper);
        //orderMapper.updateById(order);

        log.info("用户取消退款成功, 订单ID: {}, 恢复状态: {}", orderId, fromStatus);
        return true;
    }

    /**
     * 管理员审核通过退款
     * 将订单状态置为 7（已退款），支付状态置为 2（已退款），记录操作人ID
     *
     * @param orderId    订单ID
     * @param operatorId 操作人ID
     * @return 是否操作成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveRefund(Long orderId, Long operatorId) {
        log.info("管理员审核通过退款, 订单ID: {}, 操作人ID: {}", orderId, operatorId);

        // 1. 校验订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在, ID: {}", orderId);
            return false;
        }

        // 2. 校验订单状态为 6（退款中）
        if (order.getStatus() != 6) {
            log.warn("订单不在退款中状态, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        // 3. 释放库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            stockLuaScript.releaseStock(item.getSkuId(), item.getQuantity());
            inventoryRedisService.syncStockToDb(item.getSkuId());
        }

        // 4. 更新订单：status=7（已退款），pay_status=2（已退款），记录操作人
        order.setStatus(7);
        order.setPayStatus(2);
        order.setRefundOperatorId(operatorId);
        order.setRefundAmount(order.getPayAmount());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("退款审核通过完成, 订单ID: {}", orderId);
        return true;
    }

    /**
     * 管理员拒接退款
     * 将订单状态置为 8（已拒绝），记录拒绝原因、拒绝时间、操作人ID
     *
     * @param orderId      订单ID
     * @param rejectReason 拒绝原因
     * @param operatorId   操作人ID
     * @return 是否操作成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectRefund(Long orderId, String rejectReason, Long operatorId) {
        log.info("管理员拒接退款, 订单ID: {}, 原因: {}, 操作人ID: {}", orderId, rejectReason, operatorId);

        // 1. 校验订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在, ID: {}", orderId);
            return false;
        }

        // 2. 校验订单状态为 6（退款中）
        if (order.getStatus() != 6) {
            log.warn("订单不在退款中状态, 订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            return false;
        }

        // 3. 校验拒绝原因
        if (rejectReason == null || rejectReason.isEmpty()) {
            log.warn("拒接退款时拒绝原因不能为空, 订单ID: {}", orderId);
            return false;
        }

        // 4. 更新订单：status=8（已拒绝），记录拒绝信息
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
     * 根据订单状态 status 判断退款进度：未申请(0)/退款中(1)/已退款(2)/已拒绝(3)
     *
     * @param orderId 订单ID
     * @return 退款进度VO
     */
    @Override
    public RefundProgressVo getRefundProgress(Long orderId) {
        log.info("查询退款进度, 订单ID: {}", orderId);

        // 1. 校验订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在, ID: {}", orderId);
            return null;
        }

        // 2. 根据订单状态 status 判断退款进度
        RefundProgressVo vo = new RefundProgressVo();
        vo.setRefundReason(order.getRefundReason());
        vo.setRefundAmount(order.getRefundAmount());

        Integer status = order.getStatus() != null ? order.getStatus() : 0;
        switch (status) {
            case 6:
                // 退款中：用户已申请，等待管理员审核
                vo.setRefundStatusCode(1);
                vo.setRefundStatusDesc("退款中");
                vo.setApplyTime(order.getUpdatedAt());
                break;
            case 7:
                // 已退款：管理员审核通过，已完成退款
                vo.setRefundStatusCode(2);
                vo.setRefundStatusDesc("已退款");
                vo.setApplyTime(order.getUpdatedAt());
                vo.setReviewTime(order.getUpdatedAt());
                break;
            case 8:
                // 已拒绝：管理员拒接退款
                vo.setRefundStatusCode(3);
                vo.setRefundStatusDesc("已拒绝");
                vo.setRejectReason(order.getRejectReason());
                vo.setReviewTime(order.getRejectedAt());
                break;
            default:
                // 未申请退款（status 不是 6/7/8）
                vo.setRefundStatusCode(0);
                vo.setRefundStatusDesc("未申请退款");
                break;
        }

        log.info("退款进度查询结果, 订单ID: {}, 状态: {}", orderId, vo.getRefundStatusDesc());
        return vo;
    }

    /**
     * 获取订单状态描述
     *
     * @param status 订单状态
     * @return 状态描述
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
     * 获取支付状态描述
     *
     * @param payStatus 支付状态
     * @return 支付状态描述
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
     * 获取支付方式描述
     *
     * @param payType 支付方式
     * @return 支付方式描述
     */
    @Override
    public String getPayTypeDesc(String payType) {
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("alipay", "支付宝");
        typeMap.put("wechat", "微信支付");
        return typeMap.getOrDefault(payType, "未知支付方式");
    }

    /**
     * 生成订单号
     * 格式：时间戳 + 随机数
     */
    private String generateOrderNo() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis());
        sb.append(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return sb.toString();
    }
}