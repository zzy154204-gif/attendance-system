package com.example.attendance.service;

import com.example.attendance.entity.LeaveApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * 请假申请服务接口。
 */
public interface LeaveApplicationService {

    /**
     * 提交请假申请。
     */
    LeaveApplication apply(LeaveApplication application);

    /**
     * 审批请假申请。
     */
    LeaveApplication approve(Long id, boolean approved, String remark, String approver);

    /**
     * 按学生 ID 查询请假记录（分页）。
     */
    Page<LeaveApplication> findByStudent(Long studentId, Pageable pageable);

    /**
     * 按状态查询请假记录（分页）。
     */
    Page<LeaveApplication> findByStatus(String status, Pageable pageable);

    /**
     * 查询全部请假记录（分页）。
     */
    Page<LeaveApplication> findAll(Pageable pageable);

    /**
     * 按 ID 查询请假申请。
     */
    LeaveApplication findById(Long id);

    /**
     * 验证请假时间是否合法。
     */
    void validateLeaveTime(LocalDateTime startTime, LocalDateTime endTime, Long studentId);
}
