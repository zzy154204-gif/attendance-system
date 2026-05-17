package com.example.attendance.dto;

import java.util.List;

public record AttendancePageResponse(
        List<AttendanceView> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}

