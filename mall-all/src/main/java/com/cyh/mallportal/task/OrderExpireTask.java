package com.cyh.mallportal.task;

// ═══════════════════════════════════════════════════════════════
//  已废弃：功能已被 OrderExpireConsumer (延迟队列) 完全替代
//
//  替代方案：RabbitMQ 死信队列 + TTL 延迟队列
//  - OrderExpireConsumer → 订单超时到期后精准触发（毫秒级）
//  - 零 DB 轮询扫描，天然支持多实例水平扩展
//
//  迁移日期：2026-06-25
//  确认稳定后可删除此文件
// ═══════════════════════════════════════════════════════════════

// import com.cyh.mallportal.entity.Order;
// import com.cyh.mallportal.entity.OrderItem;
// import com.cyh.mallportal.mapper.OrderItemMapper;
// import com.cyh.mallportal.mapper.OrderMapper;
// import com.cyh.mallportal.service.InventoryRedisService;
// import com.cyh.mallportal.service.StockLuaScript;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;
//
// import java.time.LocalDateTime;
// import java.util.List;
//
// /**
//  * 订单超时自动取消定时任务
//  * 每 30 秒扫描一次过期未支付订单，释放冻结库存
//  * <p>
//  * ⚠️ 已废弃：由 OrderExpireConsumer（RabbitMQ 延迟队列）替代
//  */
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class OrderExpireTask {
//
//     private final OrderMapper orderMapper;
//     private final OrderItemMapper orderItemMapper;
//     private final StockLuaScript stockLuaScript;
//     private final InventoryRedisService inventoryRedisService;
//
//     /**
//      * 每 30 秒执行一次，扫描过期未支付订单
//      */
//     @Scheduled(fixedDelay = 30000)
//     @Transactional(rollbackFor = Exception.class)
//     public void releaseExpiredOrders() {
//         List<Order> expiredOrders = orderMapper.selectExpiredUnpaidOrders(LocalDateTime.now());
//         if (expiredOrders == null || expiredOrders.isEmpty()) {
//             return;
//         }
//
//         log.info("发现 {} 个过期未支付订单，开始释放库存", expiredOrders.size());
//         for (Order order : expiredOrders) {
//             try {
//                 processExpiredOrder(order);
//             } catch (Exception e) {
//                 log.error("处理过期订单失败，订单ID: {}", order.getId(), e);
//             }
//         }
//     }
//
//     /**
//      * 处理单个过期订单：释放冻结库存 + 更新订单状态
//      */
//     private void processExpiredOrder(Order order) {
//         List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
//         for (OrderItem item : items) {
//             stockLuaScript.releaseStock(item.getSkuId(), item.getQuantity());
//             inventoryRedisService.syncStockToDb(item.getSkuId());
//         }
//
//         order.setStatus(5);
//         order.setCancelReason("支付超时自动取消");
//         order.setUpdatedAt(LocalDateTime.now());
//         orderMapper.updateById(order);
//
//         log.info("过期订单自动取消成功，订单ID: {}, 订单号: {}", order.getId(), order.getOrderNo());
//     }
// }