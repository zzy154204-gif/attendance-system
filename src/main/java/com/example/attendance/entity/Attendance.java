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
@Table(name = "attendance") // 明确指定数据库表名为 attendance
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 设置自增主键
    private Long id;

    private LocalDateTime checkInTime; // 签到时间

    private String status; // 状态：比如 "正常", "迟到", "缺勤"

    // 【核心重点】建立与 Student 的关联
    @ManyToOne(fetch = FetchType.LAZY) // 设置懒加载，提高性能
    @JoinColumn(name = "student_id")   // 在数据库表中生成的物理外键列名
    private Student student;           // 直接引用 Student 对象，而不是只存一个 ID
}