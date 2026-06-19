package com.share.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;
    private final String message;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BusinessException(ResultCode resultCode, String... args) {
        super(String.format(resultCode.getMessage(), (Object[]) args));
        this.code = resultCode.getCode();
        this.message = String.format(resultCode.getMessage(), (Object[]) args);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}