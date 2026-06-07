package com.example.attendance.dao;

import com.example.attendance.entity.LeaveApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    /** 按学生 ID 查询请假记录（分页） */
    Page<LeaveApplication> findByStudent_IdOrderByApplyTimeDesc(Long studentId, Pageable pageable);

    /** 按状态查询（分页） */
    Page<LeaveApplication> findByStatusOrderByApplyTimeDesc(String status, Pageable pageable);

    /** 查询全部（分页，按申请时间倒序） */
    Page<LeaveApplication> findAllByOrderByApplyTimeDesc(Pageable pageable);

    /** 按课程和学生统计请假次数 */
    long countByStudent_IdAndCourseIdAndStatus(Long studentId, Integer courseId, String status);

    /** 统计学生在时间段内的请假次数 */
    @Query("SELECT COUNT(l) FROM LeaveApplication l WHERE l.student.id = :studentId " +
           "AND l.status = 'APPROVED' AND l.startTime <= :end AND l.endTime >= :start")
    long countApprovedOverlapping(@Param("studentId") Long studentId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}
