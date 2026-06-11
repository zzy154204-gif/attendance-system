package com.example.attendance.dao;

import com.example.attendance.entity.LeaveApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    Page<LeaveApplication> findByStudent_IdOrderByApplyTimeDesc(Long studentId, Pageable pageable);

    Page<LeaveApplication> findByStatusOrderByApplyTimeDesc(String status, Pageable pageable);

    Page<LeaveApplication> findAllByOrderByApplyTimeDesc(Pageable pageable);

    /** 按课程和学生统计请假次数 */
    long countByStudent_IdAndCourse_IdAndStatus(Long studentId, Long courseId, String status);

    /** 统计学生在时间段内的已批准请假次数（用于冲突检测） */
    @Query("SELECT COUNT(l) FROM LeaveApplication l WHERE l.student.id = :studentId " +
           "AND l.status = 'APPROVED' AND l.startTime <= :end AND l.endTime >= :start")
    long countApprovedOverlapping(@Param("studentId") Long studentId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}
