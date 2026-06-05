package com.cyh.mallcommon.exception;

/**
 * 登录失效异常
 * <p>
 * 当 Token 存在但 Redis 中对应的用户信息已过期/不存在时抛出，
 * 表示登录已失效，需要用户重新登录。
 * <p>
 * 业务码：40102 | HTTP 状态码：401
 */
public class LoginExpiredException extends RuntimeException {

    private final ErrorCode errorCode = ErrorCode.LOGIN_EXPIRED;

    public LoginExpiredException() {
        super(ErrorCode.LOGIN_EXPIRED.getMessage());
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}