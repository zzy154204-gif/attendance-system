package com.example.attendance.controller;

import com.example.attendance.dto.AttendancePageResponse;
import com.example.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 考勤查询接口。
 */
@RestController
@RequestMapping("/attendances")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 分页查询考勤记录。
     *
     * @param page 页码（从 0 开始）
     * @param size 每页数量
     * @param sortBy 排序字段
     * @param direction 排序方向（asc/desc）
     * @param studentNumber 学号过滤
     * @param status 考勤状态过滤
     * @param startTime 起始签到时间
     * @param endTime 结束签到时间
     * @return 分页结果
     */
    @GetMapping
    public AttendancePageResponse queryAttendances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String studentNumber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        // 控制层只负责接收参数并转交业务层，便于后续维护和测试。
        return attendanceService.queryAttendances(studentNumber, status, startTime, endTime, page, size, sortBy, direction);
    }
}

