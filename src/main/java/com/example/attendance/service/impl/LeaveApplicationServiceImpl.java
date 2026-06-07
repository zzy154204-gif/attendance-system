package com.example.attendance.service.impl;

import com.example.attendance.dao.LeaveApplicationRepository;
import com.example.attendance.entity.LeaveApplication;
import com.example.attendance.exception.BusinessException;
import com.example.attendance.service.LeaveApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;

    public LeaveApplicationServiceImpl(LeaveApplicationRepository leaveApplicationRepository) {
        this.leaveApplicationRepository = leaveApplicationRepository;
    }

    @Override
    @Transactional
    public LeaveApplication apply(LeaveApplication application) {
        validateLeaveTime(application.getStartTime(), application.getEndTime(),
                application.getStudent() != null ? application.getStudent().getId() : null);
        application.setStatus("PENDING");
        application.setApplyTime(LocalDateTime.now());
        return leaveApplicationRepository.save(application);
    }

    @Override
    @Transactional
    public LeaveApplication approve(Long id, boolean approved, String remark, String approver) {
        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("404", "请假申请不存在"));

        if (!"PENDING".equals(application.getStatus())) {
            throw new BusinessException("该申请已经审批过了");
        }

        application.setStatus(approved ? "APPROVED" : "REJECTED");
        application.setApprovalTime(LocalDateTime.now());
        application.setApproverRemark(remark);
        application.setApprover(approver);

        return leaveApplicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveApplication> findByStudent(Long studentId, Pageable pageable) {
        return leaveApplicationRepository.findByStudent_IdOrderByApplyTimeDesc(studentId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveApplication> findByStatus(String status, Pageable pageable) {
        return leaveApplicationRepository.findByStatusOrderByApplyTimeDesc(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveApplication> findAll(Pageable pageable) {
        return leaveApplicationRepository.findAllByOrderByApplyTimeDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveApplication findById(Long id) {
        return leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("404", "请假申请不存在"));
    }

    @Override
    public void validateLeaveTime(LocalDateTime startTime, LocalDateTime endTime, Long studentId) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("请假时间不能为空");
        }
        if (endTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("不能请假过去的日期");
        }
        if (startTime.isAfter(endTime)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }
        Duration duration = Duration.between(startTime, endTime);
        if (duration.toDays() > 3) {
            throw new BusinessException("请假时间不能超过3天");
        }
        if (studentId != null) {
            long overlapping = leaveApplicationRepository.countApprovedOverlapping(
                    studentId, startTime, endTime);
            if (overlapping > 0) {
                throw new BusinessException("该时间段已有请假记录，请勿重复请假");
            }
        }
    }
}
