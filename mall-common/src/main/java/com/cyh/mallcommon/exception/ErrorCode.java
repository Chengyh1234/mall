package com.cyh.mallcommon.exception;

/**
 * 统一错误码定义
 * <p>
 * 每个枚举项包含：业务码(businessCode)、HTTP状态码(httpStatus)、默认消息(message)
 * <p>
 * 编码规则：
 * - 4xx 系列：对应 HTTP 语义（400=参数错误，401=未认证，403=权限不足，404=资源不存在）
 * - 40001+：业务规则错误（HTTP 状态码固定为 200，由业务码标识具体错误）
 * - 5xx 系列：服务端内部错误
 */
public enum ErrorCode {

    // ========== 请求参数错误 ==========
    BAD_REQUEST(400, 400, "请求参数格式错误"),

    // ========== 认证相关错误 ==========
    NOT_LOGGED_IN(40101, 401, "未登录，请先登录"),
    AUTH_FAILED(40102, 401, "用户名或密码错误"),
    LOGIN_EXPIRED(40103, 401, "登录已失效，请重新登录"),


    // ========== 权限不足 ==========
    FORBIDDEN(403, 403, "权限不足，无法访问该资源"),

    // ========== 资源不存在 ==========
    NOT_FOUND(404, 404, "请求的资源不存在"),

    // ========== 业务规则错误 ==========
    BUSINESS_ERROR(40000, 200, "业务规则错误"),

    // ========== 系统异常 ==========
    INTERNAL_ERROR(500, 500, "服务器内部错误");

    /**
     * 业务码（返回给前端的 code）
     */
    private final int businessCode;

    /**
     * HTTP 状态码
     */
    private final int httpStatus;

    /**
     * 默认提示消息
     */
    private final String message;

    ErrorCode(int businessCode, int httpStatus, String message) {
        this.businessCode = businessCode;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getBusinessCode() {
        return businessCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}