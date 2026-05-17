package com.example.attendance.service;

import com.example.attendance.dto.AttendancePageResponse;

import java.time.LocalDateTime;

/**
 * 考勤服务接口。
 */
public interface AttendanceService {
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
}

