package com.example.attendance.service;

import com.example.attendance.dto.AttendancePageResponse;
import com.example.attendance.dto.StudentDashboardStats;
import com.example.attendance.dto.TeacherDashboardStats;
import com.example.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤服务接口。
 */
public interface AttendanceService {

    /** 获取学生端 Dashboard 统计数据 */
    StudentDashboardStats getStudentStats(Long studentId);

    /** 获取教师端 Dashboard 统计数据 */
    TeacherDashboardStats getTeacherStats();
    /**
     * 分页查询考勤记录。
     *
     * @param studentNumber 学号过滤
     * @param status 考勤状态过滤
     * @param startTime 起始签到时间
     * @param endTime 结束签到时间
     * @param page 页码（从 0 开始）
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param direction 排序方向（asc/desc）
     * @return 分页结果
     */
    AttendancePageResponse queryAttendances(
            String studentNumber,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int page,
            int size,
            String sortBy,
            String direction
    );

    /**
     * 保存考勤记录。
     *
     * @param attendance 考勤实体
     * @return 保存后的实体
     */
    Attendance saveAttendance(Attendance attendance);

    /**
     * 分页查询考勤记录（页面使用）。
     */
    Page<Attendance> findAttendancePage(
            String studentNumber,
            String status,
            Integer courseId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable
    );

    /**
     * 查询考勤记录（导出使用）。
     */
    List<Attendance> findAttendanceList(
            String studentNumber,
            String status,
            Integer courseId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}

