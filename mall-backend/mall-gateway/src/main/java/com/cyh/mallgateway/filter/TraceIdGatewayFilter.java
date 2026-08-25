package com.cyh.mallgateway.filter;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * TraceId Gateway 全局过滤器
 * <p>
 * 网关作为请求入口，在这里生成全局唯一 TraceId，
 * 注入到下游请求头 {@code X-Trace-Id}，同时写入 MDC。
 * <p>
 * 网关使用 WebFlux，因此采用 Reactor 上下文传递方式。
 */
@Component
public class TraceIdGatewayFilter implements GlobalFilter, Ordered {

    /** TraceId 请求头名称 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** MDC 中 TraceId 的 Key */
    public static final String MDC_KEY = "traceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        // 从请求头获取 TraceId，不存在则生成
        String headerTraceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        final String traceId = (headerTraceId == null || headerTraceId.isBlank())
                ? UUID.randomUUID().toString().replace("-", "")
                : headerTraceId;

        // 添加请求头传递给下游服务
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder.header(TRACE_ID_HEADER, traceId))
                .build();

        // 写入 MDC，请求结束清理
        MDC.put(MDC_KEY, traceId);
        return chain.filter(mutatedExchange)
                .doFinally(signalType -> MDC.remove(MDC_KEY));
    }

    @Override
    public int getOrder() {
        // 最高优先级，最先执行
        return Ordered.HIGHEST_PRECEDENCE;
    }
}