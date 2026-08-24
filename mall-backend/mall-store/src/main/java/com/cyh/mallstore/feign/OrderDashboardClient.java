package com.cyh.mallstore.feign;

import com.cyh.mallcommon.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单仪表盘 Feign 客户端
 * 调用 mall-order 的 OrderDashboardInternalController 获取店铺经营数据
 */
@FeignClient(name = "mall-order", path = "/api/internal/order")
public interface OrderDashboardClient {

    @GetMapping("/dashboard/store/sales")
    Result<BigDecimal> getSumCompletedSales(@RequestParam Long sellerId, @RequestParam LocalDateTime startTime);

    @GetMapping("/dashboard/store/cost")
    Result<BigDecimal> getSumCompletedCost(@RequestParam Long sellerId, @RequestParam LocalDateTime startTime);

    @GetMapping("/dashboard/store/daily-sales")
    Result<Map<String, Map<String, Object>>> getDailySales(@RequestParam Long sellerId, @RequestParam LocalDateTime startTime);

    @GetMapping("/dashboard/store/time-series")
    Result<Map<String, Map<String, Object>>> getSalesTimeSeries(
            @RequestParam Long sellerId,
            @RequestParam LocalDateTime startTime,
            @RequestParam boolean isHourly);

    @GetMapping("/dashboard/store/product-ranking")
    Result<Map<String, Map<String, Object>>> getProductRanking(
            @RequestParam Long sellerId,
            @RequestParam LocalDateTime startTime);
}