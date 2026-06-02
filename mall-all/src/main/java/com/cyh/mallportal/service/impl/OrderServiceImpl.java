package com.cyh.mallportal.service.impl;

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
import com.cyh.mallportal.vo.CartItemVo;
import com.cyh.mallportal.vo.OrderVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
     * @param userId        用户ID
     * @param orderCreateDto 订单创建DTO
     * @return 订单VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVo createOrder(Long userId, OrderCreateDto orderCreateDto) {
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
        order.setVersion(1);

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
        return getOrderById(order.getId());
    }

    /**
     * 从购物车创建订单（结算）
     * 使用 Redis Lua 脚本冻结库存，设置支付超时时间
     *
     * @param userId       用户ID
     * @param addressId    收货地址ID
     * @param payType      支付方式
     * @param buyerMessage 买家留言（可选）
     * @return 订单VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVo createOrderFromCart(Long userId, Long addressId, String payType, String buyerMessage) {
        log.info("从购物车创建订单, 用户ID: {}, 地址ID: {}, 支付方式: {}", userId, addressId, payType);

        // 1. 获取已选中的购物车商品
        List<CartItemVo> selectedItems = cartItemService.getSelectedItems(userId);
        if (selectedItems == null || selectedItems.isEmpty()) {
            log.warn("购物车中没有选中的商品");
            return null;
        }

        // 2. 获取收货地址信息
        Address address = addressMapper.selectById(addressId);
        if (address == null) {
            log.warn("收货地址不存在, {}", addressId);
            return null;
        }

        // 3. 冻结库存（原子操作，任一 SKU 失败则回滚已冻结的）
        List<Long> frozenSkuIds = new ArrayList<>();
        try {
            for (CartItemVo item : selectedItems) {
                boolean success = stockLuaScript.freezeStock(item.getSkuId(), item.getQuantity());
                if (!success) {
                    rollbackFrozenByCart(frozenSkuIds, selectedItems);
                    throw new BusinessException("商品[" + item.getProductName() + "]库存不足");
                }
                frozenSkuIds.add(item.getSkuId());
            }
        } catch (BusinessException e) {
            throw e;
        }

        // 4. 计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemVo item : selectedItems) {
            if (item.getSubtotal() != null) {
                totalAmount = totalAmount.add(item.getSubtotal());
            }
        }

        // 5. 生成订单号
        String orderNo = generateOrderNo();

        // 6. 创建订单实体
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);
        order.setStatus(1);
        order.setPayStatus(0);
        order.setPayType(payType);
        order.setRemark(buyerMessage);
        order.setExpireTime(LocalDateTime.now().plusMinutes(payExpireMinutes));

        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());

        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setVersion(1);

        orderMapper.insert(order);
        log.info("订单创建成功, 订单ID: {}, 订单号: {}, 支付截止: {}", order.getId(), orderNo, order.getExpireTime());

        // 7. 创建订单明细
        for (CartItemVo item : selectedItems) {
            Sku sku = skuMapper.selectById(item.getSkuId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderNo(orderNo);
            orderItem.setSkuId(item.getSkuId());
            orderItem.setSpuId(item.getSpuId());
            orderItem.setProductName(item.getProductName() != null ? item.getProductName() : "");
            orderItem.setProductImage(item.getProductImage() != null ? item.getProductImage() : sku.getImage());
            orderItem.setSkuSpecs(item.getSkuSpecs());
            orderItem.setPrice(item.getPrice() != null ? item.getPrice() : sku.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setTotalAmount(item.getSubtotal() != null ? item.getSubtotal() : sku.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItem.setGiftFlag(0);
            orderItem.setCreatedAt(LocalDateTime.now());

            orderItemMapper.insert(orderItem);

            // 同步 Redis 库存到 MySQL
            inventoryRedisService.syncStockToDb(item.getSkuId());
        }

        // 8. 清空已选中的购物车商品
        cartItemService.clearSelected(userId);
        log.info("已选中购物车商品已清空, 用户ID: {}", userId);

        log.info("从购物车创建订单完成, 订单ID: {}", order.getId());
        return getOrderById(order.getId());
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
    private void rollbackFrozenByCart(List<Long> frozenSkuIds, List<CartItemVo> items) {
        for (CartItemVo item : items) {
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

        OrderVo vo = new OrderVo();
        vo.setOrder(order);
        vo.setItems(items);
        vo.setDeliveries(deliveries);
        vo.setStatusDesc(getStatusDesc(order.getStatus()));
        vo.setPayStatusDesc(getPayStatusDesc(order.getPayStatus()));
        vo.setPayTypeDesc(getPayTypeDesc(order.getPayType()));

        return vo;
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
     * 根据用户ID获取订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        log.info("获取用户订单列表, 用户ID: {}", userId);
        return orderMapper.selectByUserId(userId);
    }

    /**
     * 根据用户ID和状态获取订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    @Override
    public List<Order> getOrdersByUserIdAndStatus(Long userId, Integer status) {
        log.info("获取用户订单列表, 用户ID: {}, 状态: {}", userId, status);
        return orderMapper.selectByUserIdAndStatus(userId, status);
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
        if (order == null) {
            log.warn("订单不存在, {}", orderId);
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
        if (order == null) {
            log.warn("订单不存在, {}", orderId);
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
        if (order == null) {
            log.warn("订单不存在, {}", orderId);
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
     * 删除订单
     *
     * @param orderId 订单ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long orderId) {
        log.info("删除订单, 订单ID: {}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在, {}", orderId);
            return false;
        }

        // 先删除订单明细
        orderItemMapper.deleteByOrderId(orderId);

        // 删除订单
        orderMapper.deleteById(orderId);

        log.info("订单删除成功, 订单ID: {}", orderId);
        return true;
    }

    /**
     * 获取订单明细列表
     *
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId);
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