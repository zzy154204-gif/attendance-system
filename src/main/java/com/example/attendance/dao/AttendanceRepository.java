package com.example.attendance.dao;

import com.example.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    // 根据学生 ID 查询该生的所有考勤记录
    List<Attendance> findByStudent_Id(Long studentId);

    // 根据状态查询
    List<Attendance> findByStatus(String status);

    // ========== Dashboard 统计方法 ==========

    /** 统计学生在指定时间段内的打卡次数 */
    long countByStudent_IdAndCheckInTimeBetween(Long studentId, LocalDateTime start, LocalDateTime end);

    /** 统计学生在指定时间段内某状态的打卡次数 */
    long countByStudent_IdAndStatusAndCheckInTimeBetween(Long studentId, String status, LocalDateTime start, LocalDateTime end);

    /** 统计全量在指定时间段内的打卡总次数 */
    long countByCheckInTimeBetween(LocalDateTime start, LocalDateTime end);

    /** 统计全量在指定时间段内某状态的打卡次数 */
    long countByStatusAndCheckInTimeBetween(String status, LocalDateTime start, LocalDateTime end);

    /** 按课程统计打卡次数（用于教师端各课程统计） */
    @Query("SELECT a.courseName, COUNT(a) FROM Attendance a WHERE a.checkInTime BETWEEN :start AND :end GROUP BY a.courseName")
    List<Object[]> countByCourseBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT a.student.id) FROM Attendance a WHERE a.checkInTime BETWEEN :start AND :end")
    long countDistinctStudentByCheckInTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}