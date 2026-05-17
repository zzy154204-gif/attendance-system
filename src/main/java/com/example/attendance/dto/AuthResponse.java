package com.example.attendance.dto;

/**
 * Authentication response payload without sensitive data.
 */
public record AuthResponse(
        String message,
        Integer id,
        String username,
        String realName,
        String role
) {
}

