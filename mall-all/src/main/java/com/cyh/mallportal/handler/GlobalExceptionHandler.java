package com.cyh.mallportal.handler;

import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallcommon.exception.ErrorCode;
import com.cyh.mallcommon.exception.LoginExpiredException;
import com.cyh.mallcommon.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理各类异常，返回标准 {@link Result} 格式响应，
 * 并设置对应的 HTTP 状态码。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务规则错误 ====================

    /**
     * 业务规则错误（如用户名已存在、库存不足等）
     * <p>
     * HTTP 状态码：200 | 业务码：40001
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.BUSINESS_ERROR.getHttpStatus());
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(ErrorCode.BUSINESS_ERROR.getBusinessCode(), e.getMessage());
    }

    // ==================== 找不到对应资源 ====================

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNotFound(NoHandlerFoundException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.NOT_FOUND.getHttpStatus());
        log.warn("找不到资源: {}", e.getMessage());
        return Result.error(ErrorCode.NOT_FOUND.getBusinessCode(), ErrorCode.NOT_FOUND.getMessage());
    }
    // ==================== 未认证 ====================

    /**
     * 登录认证失败（用户名或密码错误） AuthenticationException是security认证异常的父类
     * <p>
     * HTTP 状态码：401 | 业务码：40101
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<?> handleAuthenticationException(AuthenticationException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.AUTH_FAILED.getHttpStatus());
        log.warn("登录认证失败: {}", e.getMessage());
        return Result.error(ErrorCode.AUTH_FAILED.getBusinessCode(), ErrorCode.AUTH_FAILED.getMessage());
    }

    /**
     * 登录失效（Token 过期或无效）
     * <p>
     * HTTP 状态码：401 | 业务码：40102
     */
    @ExceptionHandler(LoginExpiredException.class)
    public Result<?> handleLoginExpiredException(LoginExpiredException e, HttpServletResponse response) {
        response.setStatus(e.getErrorCode().getHttpStatus());
        log.warn("登录失效: {}", e.getMessage());
        return Result.error(e.getErrorCode().getBusinessCode(), e.getMessage());
    }

    // ==================== 权限不足 ====================

    /**
     * 权限不足（@PreAuthorize 权限校验失败），接口调用的权限和方法上@PreAuthorize里进行设置的权限不一样
     * 无法处理SecurityConfig里面设置权限   AccessDeniedException是security授权异常的父类
     * <p>
     * HTTP 状态码：403 | 业务码：403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(AccessDeniedException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.FORBIDDEN.getHttpStatus());
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(ErrorCode.FORBIDDEN.getBusinessCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    // ==================== 请求参数错误 ====================

    /**
     * 参数校验失败（@Valid/@Validated 触发）
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

    // ==================== 系统异常 ====================

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