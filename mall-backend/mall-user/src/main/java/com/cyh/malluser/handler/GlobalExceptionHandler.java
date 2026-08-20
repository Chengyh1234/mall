package com.cyh.malluser.handler;

import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallcommon.exception.CaptchaInvalidException;
import com.cyh.mallcommon.exception.ErrorCode;
import com.cyh.mallcommon.exception.LoginExpiredException;
import com.cyh.mallcommon.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理各类异常，返回标准 {@link Result} 格式响应，并设置对应的 HTTP 状态码。
 * <p>
 * 认证由网关统一处理，下游服务不做认证判断。但 @PreAuthorize 在方法级别校验时
 * 仍会抛出 {@link AuthenticationException} / {@link AccessDeniedException}，
 * 在此捕获并返回统一的 JSON 响应。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验失败（@Valid / @Validated 触发）
     * <p>
     * HTTP 状态码：400 | 业务码：400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.BAD_REQUEST.getHttpStatus());
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errorMsg);
        return Result.error(ErrorCode.BAD_REQUEST.getBusinessCode(), errorMsg);
    }

    /**
     * 登录认证失败，具体包括：
     * <ul>
     *   <li>用户名或密码错误（登录时 {@link org.springframework.security.authentication.AuthenticationManager} 校验失败）</li>
     *   <li>{@code @PreAuthorize("isAuthenticated()")} 校验失败（未登录访问受保护接口）</li>
     * </ul>
     * <p>
     * HTTP 状态码：401 | 业务码：40102
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<?> handleAuthenticationException(AuthenticationException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.AUTH_FAILED.getHttpStatus());
        log.warn("登录认证失败: {}", e.getMessage());
        return Result.error(ErrorCode.AUTH_FAILED.getBusinessCode(), ErrorCode.AUTH_FAILED.getMessage());
    }

    /**
     * 权限不足（{@code @PreAuthorize} 角色校验失败）
     * <p>
     * 例如：{@code @PreAuthorize("hasRole('USER')")} 校验不通过时抛出此异常。
     * SecurityConfig 已全部放行，此异常仅来自 @PreAuthorize 方法级别校验。
     * <p>
     * HTTP 状态码：403 | 业务码：403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(AccessDeniedException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.FORBIDDEN.getHttpStatus());
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(ErrorCode.FORBIDDEN.getBusinessCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    /**
     * 登录失效（Token 过期或无效）
     * <p>
     * HTTP 状态码：401 | 业务码：40103
     */
    @ExceptionHandler(LoginExpiredException.class)
    public Result<?> handleLoginExpiredException(LoginExpiredException e, HttpServletResponse response) {
        response.setStatus(e.getErrorCode().getHttpStatus());
        log.warn("登录失效: {}", e.getMessage());
        return Result.error(e.getErrorCode().getBusinessCode(), e.getMessage());
    }

    /**
     * 验证码过期或无效（图形验证码 / 邮箱验证码不存在或已过期）
     * <p>
     * HTTP 状态码：401 | 业务码：40104
     */
    @ExceptionHandler(CaptchaInvalidException.class)
    public Result<?> handleCaptchaInvalidException(CaptchaInvalidException e, HttpServletResponse response) {
        response.setStatus(e.getErrorCode().getHttpStatus());
        log.warn("验证码过期或无效: {}", e.getMessage());
        return Result.error(e.getErrorCode().getBusinessCode(), e.getMessage());
    }

    /**
     * 请求的资源不存在（无匹配的 Controller 路由）
     * <p>
     * HTTP 状态码：404 | 业务码：404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNotFound(NoHandlerFoundException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.NOT_FOUND.getHttpStatus());
        log.warn("找不到资源: {}", e.getMessage());
        return Result.error(ErrorCode.NOT_FOUND.getBusinessCode(), ErrorCode.NOT_FOUND.getMessage());
    }

    /**
     * 业务规则错误（如用户名已存在、库存不足等）
     * <p>
     * HTTP 状态码：200 | 业务码：40000
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.BUSINESS_ERROR.getHttpStatus());
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(ErrorCode.BUSINESS_ERROR.getBusinessCode(), e.getMessage());
    }

    /**
     * 未捕获的系统异常（兜底）
     * <p>
     * HTTP 状态码：500 | 业务码：500
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletResponse response) {
        response.setStatus(ErrorCode.INTERNAL_ERROR.getHttpStatus());
        log.error("系统异常", e);
        return Result.error(ErrorCode.INTERNAL_ERROR.getBusinessCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }
}