package com.example.attendance.dto;

/**
 * 统一错误响应 DTO。
 */
public record ErrorResponse(
        String code,
        String message
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message);
    }
}
