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

    List<Attendance> findByStudent_Id(Long studentId);

    List<Attendance> findByStatus(String status);

    // ========== Dashboard 统计方法 ==========

    long countByStudent_IdAndCheckInTimeBetween(Long studentId, LocalDateTime start, LocalDateTime end);

    long countByStudent_IdAndStatusAndCheckInTimeBetween(Long studentId, String status, LocalDateTime start, LocalDateTime end);

    long countByCheckInTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndCheckInTimeBetween(String status, LocalDateTime start, LocalDateTime end);

    /** 按课程统计打卡次数（通过 course 关联查询课程名） */
    @Query("SELECT a.course.name, COUNT(a) FROM Attendance a WHERE a.checkInTime BETWEEN :start AND :end GROUP BY a.course.name")
    List<Object[]> countByCourseBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT a.student.id) FROM Attendance a WHERE a.checkInTime BETWEEN :start AND :end")
    long countDistinctStudentByCheckInTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
