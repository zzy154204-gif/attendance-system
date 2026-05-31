package com.example.attendance.dto;

/**
 * 注册页面表单对象。
 */
public record RegisterForm(
        String username,
        String password,
        String confirmPassword,
        String role
) {
}

