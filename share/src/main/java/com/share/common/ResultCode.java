package com.share.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或token无效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码（1000+）
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_EXISTS(1002, "用户名已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    CATEGORY_NOT_FOUND(1101, "分类不存在"),
    CATEGORY_IN_USE(1102, "分类已被使用，无法删除"),
    TRANSACTION_NOT_FOUND(1201, "交易记录不存在"),
    PLAN_NOT_FOUND(1301, "计划不存在"),
    PLAN_ALREADY_COMPLETED(1302, "计划已完成，无法再分配"),
    TASK_NOT_FOUND(1401, "任务不存在");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}