package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leave_application")
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 请假学生 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    /** 关联课程 ID（可选，可针对具体课程请假） */
    @Column(name = "course_id")
    private Integer courseId;

    /** 课程名称冗余字段 */
    @Column(name = "course_name", length = 100)
    private String courseName;

    /** 请假开始时间 */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /** 请假结束时间 */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /** 请假原因 */
    @Column(length = 500, nullable = false)
    private String reason;

    /** 状态：PENDING（待审批）、APPROVED（已批准）、REJECTED（已拒绝） */
    @Column(length = 20, nullable = false)
    private String status;

    /** 申请时间 */
    @Column(name = "apply_time")
    private LocalDateTime applyTime;

    /** 审批时间 */
    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    /** 审批人备注 */
    @Column(name = "approver_remark", length = 500)
    private String approverRemark;

    /** 审批人用户名 */
    @Column(name = "approver", length = 50)
    private String approver;
}
