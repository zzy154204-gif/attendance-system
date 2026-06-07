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
@Table(name = "operation_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 操作用户名 */
    @Column(nullable = false, length = 50)
    private String username;

    /** 操作类型（如：LOGIN, CHECK_IN, IMPORT, EXPORT, DELETE, APPROVE 等） */
    @Column(nullable = false, length = 30)
    private String operation;

    /** 操作目标（如：Attendance, Student, Leave 等） */
    @Column(length = 50)
    private String target;

    /** 目标 ID */
    @Column(name = "target_id")
    private Long targetId;

    /** 操作详情 */
    @Column(length = 500)
    private String detail;

    /** 操作 IP */
    @Column(length = 50)
    private String ip;

    /** 操作时间 */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /** 操作是否成功 */
    private Boolean success;
}
