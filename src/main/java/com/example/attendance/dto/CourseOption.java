package com.example.attendance.dto;

import java.time.LocalTime;

/**
 * 课程下拉选项：用于打卡与筛选。
 */
public record CourseOption(
        Integer id,
        String name,
        LocalTime startTime
) {
}

