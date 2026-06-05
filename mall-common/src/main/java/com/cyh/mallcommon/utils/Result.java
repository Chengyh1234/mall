package com.cyh.mallcommon.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用响应结果封装类
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 状态码
     * 200 表示成功
     * 其他表示失败
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 成功响应（带数据）
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 成功响应（带数据和消息）
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败响应
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> error() {
        return new Result<>(40000, "error", null);
    }

    /**
     * 失败响应（带消息）
     * @param message 错误消息
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(40000, message, null);
    }

    /**
     * 失败响应（带状态码和消息）
     * @param code 状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 链式调用 - 设置状态码
     * @param code 状态码
     * @return Result
     */
    public Result<T> code(Integer code) {
        this.code = code;
        return this;
    }

    /**
     * 链式调用 - 设置消息
     * @param message 消息
     * @return Result
     */
    public Result<T> message(String message) {
        this.message = message;
        return this;
    }

    /**
     * 链式调用 - 设置数据
     * @param data 数据
     * @return Result
     */
    public Result<T> data(T data) {
        this.data = data;
        return this;
    }
}
