package com.share.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功（无数据）
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    // 成功（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    // 失败（自定义错误码和消息）
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    // 失败（使用预定义错误码）
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    // 失败（使用预定义错误码并替换消息占位符）
    public static <T> Result<T> error(ResultCode resultCode, String... args) {
        String message = String.format(resultCode.getMessage(), (Object[]) args);
        return new Result<>(resultCode.getCode(), message, null);
    }
}