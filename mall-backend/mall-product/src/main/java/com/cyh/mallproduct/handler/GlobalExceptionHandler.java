package com.cyh.mallproduct.handler;

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
 * 商品服务全局异常处理器
 * 统一处理参数校验异常、认证授权异常、业务异常、系统异常等
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.BAD_REQUEST.getHttpStatus());
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errorMsg);
        return Result.error(ErrorCode.BAD_REQUEST.getBusinessCode(), errorMsg);
    }

    @ExceptionHandler(AuthenticationException.class)
    public Result<?> handleAuthenticationException(AuthenticationException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.AUTH_FAILED.getHttpStatus());
        log.warn("登录认证失败: {}", e.getMessage());
        return Result.error(ErrorCode.AUTH_FAILED.getBusinessCode(), ErrorCode.AUTH_FAILED.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(AccessDeniedException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.FORBIDDEN.getHttpStatus());
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(ErrorCode.FORBIDDEN.getBusinessCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    @ExceptionHandler(LoginExpiredException.class)
    public Result<?> handleLoginExpiredException(LoginExpiredException e, HttpServletResponse response) {
        response.setStatus(e.getErrorCode().getHttpStatus());
        log.warn("登录失效: {}", e.getMessage());
        return Result.error(e.getErrorCode().getBusinessCode(), e.getMessage());
    }

    @ExceptionHandler(CaptchaInvalidException.class)
    public Result<?> handleCaptchaInvalidException(CaptchaInvalidException e, HttpServletResponse response) {
        response.setStatus(e.getErrorCode().getHttpStatus());
        log.warn("验证码过期或无效: {}", e.getMessage());
        return Result.error(e.getErrorCode().getBusinessCode(), e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNotFound(NoHandlerFoundException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.NOT_FOUND.getHttpStatus());
        log.warn("找不到资源: {}", e.getMessage());
        return Result.error(ErrorCode.NOT_FOUND.getBusinessCode(), ErrorCode.NOT_FOUND.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletResponse response) {
        response.setStatus(ErrorCode.BUSINESS_ERROR.getHttpStatus());
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(ErrorCode.BUSINESS_ERROR.getBusinessCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletResponse response) {
        response.setStatus(ErrorCode.INTERNAL_ERROR.getHttpStatus());
        log.error("系统异常", e);
        return Result.error(ErrorCode.INTERNAL_ERROR.getBusinessCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }
}