package com.example.attendance.dto;

import java.util.List;
import java.util.Map;

/**
 * Dashboard 统计数据，供教师端和管理员端使用。
 */
public record TeacherDashboardStats(
        long totalStudents,          // 学生总数
        long todayCheckIns,          // 今日打卡人次
        long todayDistinctStudents,  // 今日打卡人数
        long monthTotal,             // 本月总打卡人次
        long monthNormal,            // 本月正常次数
        long monthLate,              // 本月迟到次数
        double monthRate,            // 本月出勤率（正常/总数）
        List<CourseStat> courseStats // 各课程统计
) {
    public record CourseStat(String courseName, long count) {}
}
