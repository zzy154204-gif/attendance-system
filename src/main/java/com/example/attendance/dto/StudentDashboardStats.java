package com.example.attendance.dto;

/**
 * Dashboard 统计数据，供学生端使用。
 */
public record StudentDashboardStats(
        long monthTotal,       // 本月打卡总次数
        long monthNormal,      // 本月正常次数
        long monthLate,        // 本月迟到次数
        double monthRate,      // 本月出勤率（正常/总数）
        int todayCourses,      // 今日可选课程数
        int todayCheckedIn     // 今日已打卡课程数
) {}
