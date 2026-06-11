package com.example.attendance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一 REST API 响应格式。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(200, message, null);
    }

    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
