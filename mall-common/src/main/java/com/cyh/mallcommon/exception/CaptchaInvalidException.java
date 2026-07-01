package com.cyh.mallcommon.exception;

/**
 * 验证码过期或无效异常
 * <p>
 * 当验证码不存在、已过期或无效时抛出，
 * 提示用户重新获取验证码。
 * <p>
 * 业务码：40104 | HTTP 状态码：401
 */
public class CaptchaInvalidException extends RuntimeException {

    private final ErrorCode errorCode = ErrorCode.CAPTCHA_INVALID;

    public CaptchaInvalidException() {
        super(ErrorCode.CAPTCHA_INVALID.getMessage());
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}