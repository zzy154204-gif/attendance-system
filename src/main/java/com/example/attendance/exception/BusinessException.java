package com.example.attendance.exception;

/**
 * 业务异常类，用于表示业务逻辑中的可预期错误。
 * 抛出此异常将被 GlobalExceptionHandler 统一捕获并返回友好提示。
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = "400";
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
