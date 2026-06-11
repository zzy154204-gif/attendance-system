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

    StudentDashboardStats getStudentStats(Long studentId);

    TeacherDashboardStats getTeacherStats();

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

    Attendance saveAttendance(Attendance attendance);

    Page<Attendance> findAttendancePage(
            String studentNumber,
            String status,
            Long courseId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable
    );

    List<Attendance> findAttendanceList(
            String studentNumber,
            String status,
            Long courseId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}
