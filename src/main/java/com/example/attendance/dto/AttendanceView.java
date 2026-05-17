package com.example.attendance.dto;

import java.time.LocalDateTime;

public record AttendanceView(
        Long id,
        LocalDateTime checkInTime,
        String status,
        Long studentId,
        String studentNumber,
        String studentName
) {
}

