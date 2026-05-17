package com.example.attendance.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Registration request payload.
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password,
        String realName,
        String role
) {

}

