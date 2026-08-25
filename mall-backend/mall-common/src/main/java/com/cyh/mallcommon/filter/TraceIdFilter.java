package com.cyh.mallcommon.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 过滤器
 * <p>
 * 从请求头 {@code X-Trace-Id} 提取 TraceId 写入 MDC，用于日志链路追踪。
 * 若请求头不存在则自动生成（兼容直连场景）。
 * 请求结束后清理 MDC，防止内存泄漏。
 * <p>
 * 注意：网关（mall-gateway）使用 WebFlux，不适用此 Servlet Filter，
 * 网关使用 {@code TraceIdGatewayFilter}（GlobalFilter）。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements Filter {

    /** TraceId 请求头名称 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** MDC 中 TraceId 的 Key */
    public static final String MDC_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }
            MDC.put(MDC_KEY, traceId);
            chain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}