package com.cyh.mallauth.feign;

import com.cyh.mallcommon.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单管理员统计 Feign 客户端
 * 调用 mall-order 获取平台级订单统计数据
 */
@FeignClient(name = "mall-order", path = "/api/internal/order")
public interface OrderAdminClient {

    @GetMapping("/dashboard/admin/time-series")
    Result<Map<String, Map<String, Object>>> getAdminSalesTimeSeries(
            @RequestParam LocalDateTime startTime,
            @RequestParam boolean isHourly);

    @GetMapping("/dashboard/admin/today-orders")
    Result<Long> getTodayOrders(@RequestParam LocalDateTime todayStart);

    @GetMapping("/dashboard/admin/completed-sales")
    Result<BigDecimal> getAdminCompletedSales(@RequestParam LocalDateTime startTime);
}