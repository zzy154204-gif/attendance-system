package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import java.util.List;

public interface AttendanceService {

    // 记录一条签到（学生ID + 状态）
    Attendance checkIn(Long studentId, String status);

    // 查询某学生的所有考勤记录
    List<Attendance> getByStudentId(Long studentId);

    // 查询所有考勤记录
    List<Attendance> getAll();

    // 根据状态筛选（如"迟到"、"缺勤"）
    List<Attendance> getByStatus(String status);

    // 删除一条考勤记录
    void delete(Long id);
}
