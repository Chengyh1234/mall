package com.cyh.mallcommon.exception;

/**
 * 业务异常类
 * 用于Service层抛出业务校验错误，由Controller层或全局异常处理器捕获
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}